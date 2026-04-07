package org.gupang.user.Application.Dto;

import java.util.UUID;
import org.gupang.common.entity.UserRole;
import org.gupang.user.Domain.Entity.User;
import org.gupang.user.Domain.Entity.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class getUserResponseDto {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String slackId;
    private UUID hubId;

    public static getUserResponseDto from(User user) {
        return getUserResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .slackId(user.getSlackId())
                .hubId(user.getHubId())
                .build();
    }
}
