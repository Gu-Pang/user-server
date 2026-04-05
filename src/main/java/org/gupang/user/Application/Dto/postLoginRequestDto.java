package org.gupang.user.Application.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class postLoginRequestDto {
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String username;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
}
