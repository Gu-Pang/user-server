package org.gupang.user.Presentation.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final String userId;
    private final String username;
    private final String role;
}
