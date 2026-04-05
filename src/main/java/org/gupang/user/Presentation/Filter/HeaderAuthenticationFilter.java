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
import org.gupang.user.Presentation.Dto.UserPrincipal;

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

                String userId = request.getHeader("X-User-UserId");
                String username = request.getHeader("X-User-Username");
                String userRole = request.getHeader("X-User-Role");

                log.info("Incoming Headers - UserId: {}, Username: {}, Role: {}",
                                userId, username, userRole);

                if (userId != null && username != null && userRole != null && !userRole.trim().isEmpty()) {
                        userRole = userRole.trim();
                        if (!userRole.startsWith("ROLE_")) {
                                userRole = "ROLE_" + userRole;
                        }
                        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(userRole));
                        // null 부분은 credentials
                        // UserPrincipal 객체 생성하여 Principal로 사용
                        UserPrincipal userPrincipal = new UserPrincipal(userId, username, userRole);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        userPrincipal, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Auth Success Final: userPrincipal=[{}], authorities=[{}]",
                                        userPrincipal, authorities);
                }

                filterChain.doFilter(request, response);
        }
}
