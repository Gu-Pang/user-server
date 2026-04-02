package org.gupang.user.Infrastructure.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.gupang.user.Domain.Entity.User;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;

//p_user와 keycloak 동기화용
@Slf4j
@Component
public class UserEntitySyncListener {

    private static Keycloak keycloakAdmin;
    private static String realm;

    @Autowired
    public void init(Keycloak keycloakAdmin, @Value("${keycloak.realm}") String realm) {
        UserEntitySyncListener.keycloakAdmin = keycloakAdmin;
        UserEntitySyncListener.realm = realm;
    }

    @PostPersist
    @PostUpdate
    public void syncToKeycloak(User user) {
        if (user.getKeycloakId() == null) {
            return;
        }

        try {
            log.info("Syncing user {} to Keycloak...", user.getUsername());
            UserRepresentation kcUser = keycloakAdmin.realm(realm).users().get(user.getKeycloakId()).toRepresentation();

            kcUser.setFirstName(user.getFirstName());
            kcUser.setLastName(user.getLastName());
            kcUser.setEmail(user.getEmail());

            // kcUser.getAttributes()가 null일경우 put하면 에러가 뜬다하여 hashmap 생성하여 다시 kcUser에 할당
            Map<String, List<String>> attributes = new HashMap<>();
            if (kcUser.getAttributes() != null) {
                attributes.putAll(kcUser.getAttributes());
            }

            String roleValue = user.getRole().name().toUpperCase();
            attributes.put("role", List.of(roleValue));
            kcUser.setAttributes(attributes);

            keycloakAdmin.realm(realm).users().get(user.getKeycloakId()).update(kcUser);
        } catch (Exception e) {
            log.error("Failed to sync user {} to Keycloak: {}", user.getUsername(), e.getMessage());
        }
    }
}
