package org.gupang.user.Infrastructure.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

//EntityListeners에서 auditorAware bean 사용
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null) {
                return Optional.empty();
            }

            // TODO : 일단은 keycloakId로 설정해두었는데, 나중에 p_user의 userId로 변경해야 할듯한데
            String userId = attributes.getRequest().getHeader("X-User-KeycloakId");
            if (userId == null || userId.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of(UUID.fromString(userId));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        };
    }
}
