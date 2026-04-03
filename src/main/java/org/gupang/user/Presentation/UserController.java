package org.gupang.user.Presentation;

import java.util.Map;
import java.util.UUID;

import org.gupang.user.Application.UserService;
import org.gupang.user.Application.Dto.postLoginRequestDto;
import org.gupang.user.Application.Dto.postLoginResponseDto;
import org.gupang.user.Application.Dto.postSignUpRequestDto;
import org.gupang.user.Application.Dto.postSignUpResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// gateway 목록 /api/v1/admin/**, /api/v1/users/**, /api/v1/auth/**
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 업체 유저 가입 신청
    @PostMapping("/users")
    public ResponseEntity<postSignUpResponseDto> signUpCompany(@RequestBody postSignUpRequestDto requestDto) {
        requestDto.setRole("COMPANY");
        return ResponseEntity.ok(userService.signUp(requestDto));
    }

    // 유저 생성
    @PostMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_MASTER')") // 오직 MASTER 권한자만 가능, 허브 관리자거는 따로 만들어야 할듯
    public ResponseEntity<postSignUpResponseDto> signUpMaster(@RequestBody postSignUpRequestDto requestDto) {
        if (requestDto.getRole() == null || requestDto.getRole().isBlank()) {
            requestDto.setRole("DELIVERY");
        }
        return ResponseEntity.ok(userService.signUp(requestDto));
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<postLoginResponseDto> login(@RequestBody postLoginRequestDto requestDto,
            @RequestHeader Map<String, String> headers) {
        // log.info(responseDto.toString());
        log.info("headers: {}", headers);
        return ResponseEntity.ok(userService.login(requestDto));
    }

    // 로그인 테스트용
    @GetMapping("/auth/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getMyInfo(@AuthenticationPrincipal String username,
            @RequestHeader Map<String, String> headers) {
        log.info("헤더들: {}", headers);
        return ResponseEntity.ok("로그인 ID : " + username);
    }

    // 유저 삭제
    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

}