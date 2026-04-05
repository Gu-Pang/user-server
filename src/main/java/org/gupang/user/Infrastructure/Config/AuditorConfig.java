package org.gupang.user.Infrastructure.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditorConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()
                    || authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.empty();
            }

            // HeaderAuthenticationFilter에서 Principal에 userId(UUID 문자열)를 담음
            Object principal = authentication.getPrincipal();
            if (principal instanceof String userIdStr) {
                try {
                    return Optional.of(UUID.fromString(userIdStr));
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
            }

            return Optional.empty();
        };
    }
}
