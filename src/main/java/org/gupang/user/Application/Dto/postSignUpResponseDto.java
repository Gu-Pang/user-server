package org.gupang.user.Application.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gupang.common.entity.UserRole;
import org.gupang.user.Domain.Entity.UserStatus;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class postSignUpResponseDto {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String slackId;
    private String keycloakId;
}
