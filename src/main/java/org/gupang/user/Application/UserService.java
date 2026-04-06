package org.gupang.user.Application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gupang.user.Application.Dto.*;
import org.gupang.user.Domain.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

import feign.FeignException;

import org.gupang.common.entity.UserRole;
import org.gupang.user.Domain.Entity.User;
import org.gupang.user.Domain.Entity.UserStatus;
import org.gupang.user.Infrastructure.External.KeycloakService;

import org.gupang.common.exception.CustomException;
import org.gupang.common.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakService keycloakService;

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
    public postSignUpResponseDto signUp(postSignUpRequestDto requestDto) {
        UserRole userRole = requestDto.getRole();
        UserStatus initialStatus = UserStatus.fromRole(userRole);

        // Keycloak user 생성
        String keycloakId = keycloakService.createUser(requestDto);

        try {
            // DTO->Entity
            User user = requestDto.toEntity(
                    passwordEncoder.encode(requestDto.getPassword()),
                    userRole,
                    initialStatus,
                    UUID.fromString(keycloakId));

            // Entity->DTO
            return postSignUpResponseDto.from(userRepository.save(user));
        } catch (Exception e) {
            log.error("Failed to save user in DB. Rolling back Keycloak user: {}", keycloakId, e);
            keycloakService.deleteUser(keycloakId);
            throw e;
        }
    }

    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        keycloakService.disableUser(user.getUserId().toString());

        userRepository.delete(user);
    }

}
