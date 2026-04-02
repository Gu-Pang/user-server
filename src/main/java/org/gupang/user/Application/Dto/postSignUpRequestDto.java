package org.gupang.user.Application.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class postSignUpRequestDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
