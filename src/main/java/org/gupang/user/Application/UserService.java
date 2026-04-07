package org.gupang.user.Application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gupang.user.Application.Dto.postUserRequestDto;
import org.gupang.user.Application.Dto.postUserResponseDto;
import org.gupang.user.Application.Dto.postDeliveryManagerRequestDto;
import org.gupang.user.Application.Dto.putUserRequestDto;
import org.gupang.user.Application.Dto.putUserResponseDto;
import org.gupang.user.Application.Dto.getUserResponseDto;
import org.gupang.user.Application.Dto.getUsersResponseDto;
import org.gupang.user.Application.Dto.patchUserRequestDto;
import org.gupang.user.Application.Dto.patchUserResponseDto;
import org.gupang.user.Application.Dto.postLoginRequestDto;
import org.gupang.user.Application.Dto.postLoginResponseDto;
import org.gupang.user.Application.Dto.patchDeliveryManagerRequestDto;
import org.gupang.user.Application.Dto.patchDeliveryManagerResponseDto;
import org.gupang.user.Application.Dto.getDeliveryManagerResponseDto;
import org.gupang.user.Application.Dto.updateDeliveryManagerRequestDto;
import org.gupang.user.Presentation.Dto.UserPrincipal;
import org.gupang.user.Domain.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import feign.FeignException;

import org.gupang.common.entity.UserRole;
import org.gupang.user.Domain.Entity.DeliveryManager;
import org.gupang.user.Domain.Entity.User;
import org.gupang.user.Domain.Entity.UserStatus;
import org.gupang.user.Domain.Entity.DeliveryType;
import org.gupang.user.Domain.Entity.DeliveryStatus;
import org.gupang.user.Domain.Repository.DeliveryManagerRepository;
import org.gupang.user.Infrastructure.Client.HubClient;
import org.gupang.user.Infrastructure.External.KeycloakService;

import org.gupang.common.exception.CustomException;
import org.gupang.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakService keycloakService;
    private final HubClient hubClient;

    public postLoginResponseDto login(postLoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        user.validateUserStatus();

        // Keycloak getToken
        Map<String, Object> tokenResponse;
        try {
            tokenResponse = keycloakService.getToken(requestDto.getUsername(), requestDto.getPassword());
        } catch (FeignException.Unauthorized e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        } catch (FeignException.BadRequest e) {
            log.error("Keycloak 400 Bad Request: {}", e.contentUTF8());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Keycloak login error: ", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // Map->DTO
        return postLoginResponseDto.of(tokenResponse);
    }

    @Transactional
    public postUserResponseDto signUp(postUserRequestDto requestDto, UserPrincipal principal) {
        UserRole userRole = requestDto.getRole();
        UserStatus initialStatus = UserStatus.fromRole(userRole);

        // null은 업체 유저 가입
        if (principal != null) {
            UUID adminId = UUID.fromString(principal.getUserId());
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

            // 허브 관리자
            if (admin.getRole() == UserRole.MANAGER) {

                if (admin.getHubId() == null || !admin.getHubId().equals(requestDto.getHubId())) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }

                if (userRole == UserRole.MASTER || userRole == UserRole.MANAGER) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }
            }
        }

        postDeliveryManagerRequestDto deliveryDto = requestDto.getDeliveryInfo();
        DeliveryType deliveryType = (deliveryDto != null && deliveryDto.getDeliveryType() != null)
                ? deliveryDto.getDeliveryType()
                : DeliveryType.HUB;

        if (userRole == UserRole.MANAGER || userRole == UserRole.DELIVERY) {
            if (requestDto.getHubId() == null) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }

        if (requestDto.getHubId() != null) {
            try {
                // 사실상 여기서는 필요는 없는 기능인 것 같긴한데 가져오는거 해보려고 약간 검증 역할처럼 넣음
                hubClient.getHub(requestDto.getHubId());
            } catch (Exception e) {
                throw new CustomException(ErrorCode.NOT_FOUND);
            }
        }

        // Keycloak user 생성
        String keycloakId = keycloakService.createUser(requestDto);

        try {
            // DTO->Entity
            User user = requestDto.toEntity(
                    passwordEncoder.encode(requestDto.getPassword()),
                    userRole,
                    initialStatus,
                    UUID.fromString(keycloakId));

            User savedUser = userRepository.save(user);

            // 배송 담당자인 경우 p_delivery_manager에 데이터 생성
            if (userRole == UserRole.DELIVERY && requestDto.getHubId() != null) {
                Integer currentMax = deliveryManagerRepository.findMaxSequenceByDeliveryType(deliveryType);
                int nextSeq = (currentMax != null ? currentMax + 1 : 0);

                postDeliveryManagerRequestDto finalDto = (deliveryDto != null) ? deliveryDto
                        : new postDeliveryManagerRequestDto();
                DeliveryManager dm = finalDto.toEntity(
                        savedUser.getUserId(),
                        savedUser.getHubId(),
                        nextSeq,
                        DeliveryStatus.AVAILABLE);

                deliveryManagerRepository.save(dm);
            }

            // Entity->DTO
            return postUserResponseDto.from(savedUser);
        } catch (Exception e) {
            log.error("Failed to save user in DB. Rolling back Keycloak user: {}", keycloakId, e);
            keycloakService.deleteUser(keycloakId);
            throw e;
        }
    }

    // 유저 목록 조회
    public Page<getUsersResponseDto> getUsers(UserPrincipal principal, UserRole role, UUID hubId,
            Pageable pageable) {
        UUID adminId = UUID.fromString(principal.getUserId());
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 허브 관리자는 자신의 허브 ID로 검색 조건을 강제 고정
        if (admin.getRole() == UserRole.MANAGER) {
            hubId = admin.getHubId();
        }

        if (hubId == null) {
            return userRepository.findAllByRole(role, pageable)
                    .map(getUsersResponseDto::from);
        }

        return userRepository.findAllByRoleAndHubId(role, hubId, pageable)
                .map(getUsersResponseDto::from);
    }

    // 유저 상세 정보 수정
    @Transactional
    public putUserResponseDto updateUserInfo(UUID userId, putUserRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        user.setSlackId(requestDto.getSlackId());

        return putUserResponseDto.from(userRepository.save(user));
    }

    // 업체 유저 승인
    @Transactional
    public patchUserResponseDto approveCompanyUser(UUID adminUserId, UUID targetUserId,
            patchUserRequestDto requestDto) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 허브 관리자
        if (admin.getRole() == UserRole.MANAGER) {
            if (targetUser.getRole() == UserRole.DELIVERY || targetUser.getRole() == UserRole.COMPANY) {
                if (targetUser.getHubId() != null && !targetUser.getHubId().equals(admin.getHubId())) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }
            }
        }

        targetUser.setStatus(UserStatus.APPROVED);
        return patchUserResponseDto.from(userRepository.save(targetUser));
    }

    // 배송 정보 상세 조회
    public getDeliveryManagerResponseDto getDeliveryInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        DeliveryManager dm = deliveryManagerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return getDeliveryManagerResponseDto.from(dm, user.getHubId());
    }

    // 배송 정보 설정 (허브 관리자용)
    @Transactional
    public patchDeliveryManagerResponseDto updateDeliverySettings(UUID userId,
            patchDeliveryManagerRequestDto requestDto, UserPrincipal principal) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // [보안] 허브 관리자 권한 체크 (Controller에서 인증을 강제하므로 principal은 항상 존재함)
        UUID adminId = UUID.fromString(principal.getUserId());
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 허브 관리자
        if (admin.getRole() == UserRole.MANAGER) {
            if (targetUser.getHubId() == null || !targetUser.getHubId().equals(admin.getHubId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        }

        DeliveryManager dm = deliveryManagerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (requestDto.getDeliveryType() != null) {
            dm.setDeliveryType(DeliveryType.valueOf(requestDto.getDeliveryType()));
        }
        if (requestDto.getSequence() != null) {
            dm.setSequence(requestDto.getSequence());
        }
        if (requestDto.getStatus() != null) {
            dm.setStatus(DeliveryStatus.valueOf(requestDto.getStatus()));
        }

        return patchDeliveryManagerResponseDto.from(deliveryManagerRepository.save(dm), targetUser.getHubId());
    }

    // 배송 담당자 상태 및 순번 업데이트(타 서버용)
    @Transactional
    public patchDeliveryManagerResponseDto updateDeliveryManager(UUID userId,
            updateDeliveryManagerRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        DeliveryManager dm = deliveryManagerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (requestDto.getStatus() != null) {
            dm.setStatus(requestDto.getStatus());
        }
        if (requestDto.getSequence() != null) {
            dm.setSequence(requestDto.getSequence());
        }

        return patchDeliveryManagerResponseDto.from(deliveryManagerRepository.save(dm), user.getHubId());
    }

    // 배송 담당자 조회 (타 서버용)
    public List<getDeliveryManagerResponseDto> getDeliveryManagersByHubAndSettings(UUID hubId, DeliveryType type,
            Integer sequence, DeliveryStatus status) {
        return deliveryManagerRepository.search(hubId, type, sequence, status).stream()
                .map(dm -> getDeliveryManagerResponseDto.from(dm, dm.getHubId()))
                .toList();
    }

    // 유저 상세 정보 조회
    public getUserResponseDto getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return getUserResponseDto.from(user);
    }

    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        keycloakService.disableUser(user.getUserId().toString());

        deliveryManagerRepository.findByUserId(userId).ifPresent(deliveryManagerRepository::delete);

        userRepository.delete(user);
    }
}
