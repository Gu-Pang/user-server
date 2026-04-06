package org.gupang.user.Infrastructure.External;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gupang.common.exception.CustomException;
import org.gupang.common.exception.ErrorCode;
import org.gupang.user.Application.Dto.postUserRequestDto;
import org.gupang.user.Infrastructure.Client.KeycloakClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloakAdmin;
    private final KeycloakClient keycloakClient;

    private static final String KC_ROLE_ATTRIBUTE = "role";
    private static final int KC_CREATION_SUCCESS_STATUS = 201;

    // OAuth2 parameters
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    // Keycloak token 가져오기 (로그인) form형태로 보내야함
    public Map<String, Object> getToken(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(GRANT_TYPE, GRANT_TYPE_PASSWORD);
        formData.add(CLIENT_ID, clientId);
        formData.add(CLIENT_SECRET, clientSecret);
        formData.add(USERNAME, username);
        formData.add(PASSWORD, password);

        return keycloakClient.getToken(formData);
    }

    // Keycloak user 생성
    public String createUser(postUserRequestDto requestDto) {
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(requestDto.getUsername());
        kcUser.setEmail(requestDto.getEmail());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);
        kcUser.setFirstName(requestDto.getFirstName());
        kcUser.setLastName(requestDto.getLastName());
        kcUser.setRequiredActions(Collections.emptyList());
        kcUser.setAttributes(Map.of(KC_ROLE_ATTRIBUTE, List.of(requestDto.getRole().name())));

        RealmResource realmResource = keycloakAdmin.realm(realm);
        Response response = realmResource.users().create(kcUser);

        if (response.getStatus() != KC_CREATION_SUCCESS_STATUS) {
            log.error("Keycloak user creation failed. Status: {}, Info: {}",
                    response.getStatus(), response.getStatusInfo());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // Location이라는거는 보통 URL을 반환한다고 함. Keycloak은 마지막 경로에 keycloakId를 반환한다고 함
        String location = response.getHeaderString("Location");
        String keycloakId = location.substring(location.lastIndexOf("/") + 1);

        // 비밀번호 설정
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(requestDto.getPassword());
        credential.setTemporary(false);

        realmResource.users().get(keycloakId).resetPassword(credential);

        return keycloakId;
    }

    // Keycloak user 비활성화 (Soft Delete용)
    public void disableUser(String keycloakId) {
        if (keycloakId == null)
            return;

        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserRepresentation kcUser = realmResource.users().get(keycloakId).toRepresentation();
            kcUser.setEnabled(false);
            realmResource.users().get(keycloakId).update(kcUser);
        } catch (Exception e) {
            log.error("Failed to disable user in Keycloak: {}", keycloakId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // Keycloak user 삭제 (signup 트랜잭션 롤백용. keycloakId를 먼저 가져와야되서 순서 바꾸는거로는 안됨)
    public void deleteUser(String keycloakId) {
        if (keycloakId == null)
            return;

        try {
            keycloakAdmin.realm(realm).users().get(keycloakId).remove();
        } catch (Exception e) {
            log.error("Failed to delete user in Keycloak for rollback: {}", keycloakId, e);
        }
    }
}
