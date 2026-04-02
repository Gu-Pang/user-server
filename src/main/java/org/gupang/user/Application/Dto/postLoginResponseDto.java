package org.gupang.user.Application.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class postLoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
}
