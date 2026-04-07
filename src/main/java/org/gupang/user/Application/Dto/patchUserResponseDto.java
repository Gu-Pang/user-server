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
public class patchUserResponseDto {
    private UUID userId;
    private String username;
    private UserRole role;
    private UserStatus status;

    public static patchUserResponseDto from(User user) {
        return patchUserResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
