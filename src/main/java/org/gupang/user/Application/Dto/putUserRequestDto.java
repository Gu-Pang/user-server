package org.gupang.user.Application.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class putUserRequestDto {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String firstName;

    @NotBlank(message = "성은 필수 입력 값입니다.")
    private String lastName;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "Slack ID는 필수 입력 값입니다.")
    private String slackId;
}
