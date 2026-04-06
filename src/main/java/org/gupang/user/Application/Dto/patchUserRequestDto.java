package org.gupang.user.Application.Dto;

import org.gupang.user.Domain.Entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class patchUserRequestDto {
    private UserStatus status;
}
