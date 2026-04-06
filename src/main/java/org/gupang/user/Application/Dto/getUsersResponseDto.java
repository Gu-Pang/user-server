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
public class getUsersResponseDto {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;
    private UUID hubId;

    public static getUsersResponseDto from(User user) {
        return getUsersResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .status(user.getStatus())
                .hubId(user.getHubId())
                .build();
    }
}
