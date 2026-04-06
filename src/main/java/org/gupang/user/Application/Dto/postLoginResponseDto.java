package org.gupang.user.Application.Dto;

import java.util.Map;

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

    public static postLoginResponseDto of(Map<String, Object> tokenResponse) {
        return postLoginResponseDto.builder()
                .accessToken(String.valueOf(tokenResponse.get("access_token")))
                .refreshToken(String.valueOf(tokenResponse.get("refresh_token")))
                .expiresIn(Long.parseLong(String.valueOf(tokenResponse.get("expires_in"))))
                .tokenType(String.valueOf(tokenResponse.get("token_type")))
                .build();
    }
}
