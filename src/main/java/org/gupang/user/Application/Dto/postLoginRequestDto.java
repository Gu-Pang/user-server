package org.gupang.user.Application.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class postLoginRequestDto {
    private String username;
    private String password;
}
