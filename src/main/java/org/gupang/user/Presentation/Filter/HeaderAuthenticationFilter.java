package org.gupang.user.Presentation.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                String keycloakId = request.getHeader("X-User-KeycloakId");
                String username = request.getHeader("X-User-Username");
                String userRole = request.getHeader("X-User-Role");

                log.info("Incoming Headers - KeycloakId: {}, Username: {}, Role: {}",
                                keycloakId, username, userRole);

                if (keycloakId != null && username != null && userRole != null && !userRole.trim().isEmpty()) {
                        userRole = userRole.trim();
                        if (!userRole.startsWith("ROLE_")) {
                                userRole = "ROLE_" + userRole;
                        }
                        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(userRole));

                        // null 부분은 credentials
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        username, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Auth Success Final: user=[{}], keycloakId=[{}], authorities=[{}]",
                                        username, keycloakId, authorities);
                }

                filterChain.doFilter(request, response);
        }
}
