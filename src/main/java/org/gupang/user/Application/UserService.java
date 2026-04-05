package org.gupang.user.Application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gupang.user.Application.Dto.postLoginRequestDto;
import org.gupang.user.Application.Dto.postLoginResponseDto;
import org.gupang.user.Domain.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.gupang.user.Infrastructure.Client.KeycloakClient;
import feign.FeignException;

import org.gupang.common.entity.UserRole;
import org.gupang.user.Application.Dto.postSignUpRequestDto;
import org.gupang.user.Application.Dto.postSignUpResponseDto;
import org.gupang.user.Domain.Entity.User;
import org.gupang.user.Domain.Entity.UserStatus;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import jakarta.ws.rs.core.Response;

import org.gupang.common.exception.CustomException;
import org.gupang.common.exception.ErrorCode;
import org.gupang.user.Domain.Exception.UserErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloakAdmin;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakClient keycloakClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    public postLoginResponseDto login(postLoginRequestDto requestDto) {
        // DB에서 사용자 정보 조회
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // Keycloak을 통한 로그인
        Map<String, Object> tokenResponse;
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("username", requestDto.getUsername());
            formData.add("password", requestDto.getPassword());

            tokenResponse = keycloakClient.getToken(formData);
        } catch (FeignException.Unauthorized e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        } catch (FeignException.BadRequest e) {
            log.error("Keycloak 400 Bad Request: {}", e.contentUTF8());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Keycloak login error: ", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 승인 상태 체크
        if (user.getStatus() == UserStatus.PENDING) {
            throw new CustomException(UserErrorCode.PENDING_APPROVAL);
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new CustomException(UserErrorCode.REJECTED_USER);
        }

        // 응답 DTO 반환
        return postLoginResponseDto.builder()
                .accessToken(String.valueOf(tokenResponse.get("access_token")))
                .refreshToken(String.valueOf(tokenResponse.get("refresh_token")))
                .expiresIn(Long.parseLong(String.valueOf(tokenResponse.get("expires_in"))))
                .tokenType(String.valueOf(tokenResponse.get("token_type")))
                .build();
    }

    @Transactional
    public postSignUpResponseDto signUp(postSignUpRequestDto requestDto) {
        // Role 결정 및 초기 Status 결정
        UserRole userRole = UserRole.valueOf(requestDto.getRole().toUpperCase());
        UserStatus initialStatus = UserStatus.fromRole(userRole);

        // 로컬 DB 유저 객체 생성 (Keycloak ID는 나중에 set)
        User user = User.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .role(userRole)
                .status(initialStatus)
                .build();

        // 3. Keycloak용 User Representation 생성
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(requestDto.getUsername());
        kcUser.setEmail(requestDto.getEmail());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);
        kcUser.setFirstName(requestDto.getFirstName());
        kcUser.setLastName(requestDto.getLastName());
        kcUser.setRequiredActions(Collections.emptyList());

        // Keycloak 유저 속성(Attributes)에 Role 추가 (JWT Mapper에서 사용됨)
        kcUser.setAttributes(Map.of("role", List.of(requestDto.getRole().toUpperCase())));

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(requestDto.getPassword());
        credential.setTemporary(false);
        // kcUser.setCredentials(Collections.singletonList(credential)); // 초기 생성 시 인증
        // 정보는 제외 (충돌 방지)

        // 4. Keycloak API 호출
        RealmResource realmResource = keycloakAdmin.realm(realm);
        Response response = realmResource.users().create(kcUser);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Keycloak 사용자 생성 실패: " + response.getStatusInfo());
        }

        // 5. 생성된 Keycloak ID 추출
        String location = response.getHeaderString("Location");
        String keycloakId = location.substring(location.lastIndexOf("/") + 1);
        user.setKeycloakId(keycloakId);

        // 6. 비밀번호 명시적 설정 (최신 Keycloak 버전 호환성)
        credential.setTemporary(false);
        realmResource.users().get(keycloakId).resetPassword(credential);

        User savedUser = userRepository.save(user);

        return postSignUpResponseDto.builder()
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .slackId(savedUser.getSlackId())
                .keycloakId(savedUser.getKeycloakId())
                .build();
    }

    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 1. Keycloak 삭제
        if (user.getKeycloakId() != null) {
            keycloakAdmin.realm(realm).users().get(user.getKeycloakId()).remove();
        }

        // 2. 로컬 DB 삭제
        userRepository.delete(user);
    }

}
