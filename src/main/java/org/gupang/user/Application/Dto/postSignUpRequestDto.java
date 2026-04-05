package org.gupang.user.Application.Dto;

import java.util.UUID;
import org.gupang.common.entity.UserRole;
import org.gupang.user.Domain.Entity.User;
import org.gupang.user.Domain.Entity.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class postSignUpRequestDto {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "아이디는 4자 이상 10자 이하로 입력해주세요. (알파벳 소문자, 숫자만 가능)")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+={}\\[\\]|\\\\:;\"'<>,.?/~`-]{8,15}$", message = "비밀번호는 8자 이상 15자 이하로 입력해주세요. (알파벳 대소문자, 숫자, 특수문자 가능)")
    private String password;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String firstName;

    @NotBlank(message = "성은 필수 입력 값입니다.")
    private String lastName;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private UserRole role;

    @NotBlank(message = "Slack ID는 필수 입력 값입니다.")
    private String slackId;

    public User toEntity(String encodedPassword,
            UserRole userRole,
            UserStatus initialStatus,
            UUID userId) {
        return User.builder()
                .userId(userId)
                .username(this.username)
                .password(encodedPassword)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .role(userRole)
                .status(initialStatus)
                .slackId(this.slackId)
                .build();
    }
}
