package org.gupang.user.Presentation;

import java.util.Map;
import java.util.List;
import java.util.UUID;

import org.gupang.common.entity.UserRole;
import org.gupang.user.Domain.Entity.DeliveryStatus;
import org.gupang.user.Domain.Entity.DeliveryType;
import org.gupang.user.Application.UserService;
import org.gupang.user.Application.Dto.postUserRequestDto;
import org.gupang.user.Application.Dto.postUserResponseDto;
import org.gupang.user.Application.Dto.putUserRequestDto;
import org.gupang.user.Application.Dto.putUserResponseDto;
import org.gupang.user.Application.Dto.getUserResponseDto;
import org.gupang.user.Application.Dto.getUsersResponseDto;
import org.gupang.user.Application.Dto.patchUserRequestDto;
import org.gupang.user.Application.Dto.patchUserResponseDto;
import org.gupang.user.Application.Dto.postLoginRequestDto;
import org.gupang.user.Application.Dto.postLoginResponseDto;
import org.gupang.user.Application.Dto.updateDeliveryManagerRequestDto;
import org.gupang.user.Presentation.Dto.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.gupang.user.Application.Dto.getDeliveryManagerResponseDto;
import org.gupang.user.Application.Dto.patchDeliveryManagerRequestDto;
import org.gupang.user.Application.Dto.patchDeliveryManagerResponseDto;

import jakarta.validation.Valid;
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
    public ResponseEntity<postUserResponseDto> signUpCompany(@Valid @RequestBody postUserRequestDto requestDto) {
        requestDto.setRole(UserRole.COMPANY);
        return ResponseEntity.ok(userService.signUp(requestDto, null));
    }

    // 유저 생성
    @PostMapping("/admin/users")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'ROLE_MANAGER')")
    public ResponseEntity<postUserResponseDto> signUpMaster(@Valid @RequestBody postUserRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.signUp(requestDto, principal));
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<postLoginResponseDto> login(@Valid @RequestBody postLoginRequestDto requestDto,
            @RequestHeader Map<String, String> headers) {
        // log.info(responseDto.toString());
        log.info("headers: {}", headers);
        return ResponseEntity.ok(userService.login(requestDto));
    }

    // 로그인 테스트용
    @GetMapping("/auth/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getMyInfo(@AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader Map<String, String> headers) {
        log.info("헤더들: {}", headers);
        return ResponseEntity.ok("UUID : " + principal.getUserId()
                + " / 로그인 ID : " + principal.getUsername());
    }

    // 유저 삭제
    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    // 업체 유저 승인
    @PatchMapping("/admin/users/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'ROLE_MANAGER')")
    public ResponseEntity<patchUserResponseDto> approveCompanyUser(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userId, @RequestBody patchUserRequestDto requestDto) {
        return ResponseEntity
                .ok(userService.approveCompanyUser(UUID.fromString(principal.getUserId()), userId, requestDto));
    }

    // 유저 목록 조회
    @GetMapping("/admin/users")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'ROLE_MANAGER')")
    public ResponseEntity<Page<getUsersResponseDto>> getUsers(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UUID hubId,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(principal, role, hubId, pageable));
    }

    // 유저 상세 정보 수정
    @PutMapping("/users/{userId}")
    @PreAuthorize("#userId.toString() == principal.userId or hasRole('MASTER')")
    public ResponseEntity<putUserResponseDto> updateUserInfo(@PathVariable UUID userId,
            @RequestBody putUserRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUserInfo(userId, requestDto));
    }

    // 유저 상세 정보 조회
    @GetMapping("/users/{userId}")
    @PreAuthorize("#userId.toString() == principal.userId or hasRole('MASTER')")
    public ResponseEntity<getUserResponseDto> getUserInfo(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    // 배송자 배송 정보 조회
    @GetMapping("/users/{userId}/delivery")
    @PreAuthorize("#userId.toString() == principal.userId or hasRole('MASTER')")
    public ResponseEntity<getDeliveryManagerResponseDto> getDeliveryInfo(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getDeliveryInfo(userId));
    }

    // 배송자 배송 정보 설정
    @PatchMapping("/users/{userId}/delivery-settings")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'ROLE_MANAGER')")
    public ResponseEntity<patchDeliveryManagerResponseDto> updateDeliverySettings(@PathVariable UUID userId,
            @RequestBody patchDeliveryManagerRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.updateDeliverySettings(userId, requestDto, principal));
    }

    // 배송 담당자 상태 및 순번 업데이트 (타 서버용)
    // TODO 개발 시간 부족으로 PreAuthorize 없애고 권한 관련 로직 일단 없이 진행함 (근데 로그인 자체는 하고 테스트 해야할듯)
    // patch라서 에러날수도 있을듯
    @PatchMapping("/admin/delivery-managers/{userId}")
    public ResponseEntity<patchDeliveryManagerResponseDto> updateDeliveryManager(@PathVariable UUID userId,
            @RequestBody updateDeliveryManagerRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateDeliveryManager(userId, requestDto));
    }

    // 배송 담당자 조회 (타 서버용)
    // TODO 개발 시간 부족 으로 PreAuthorize 없애고 권한 관련 로직 일단 없이 진행함 (근데 로그인 자체는 하고 테스트 해야할듯)
    @GetMapping("/admin/delivery-managers")
    public ResponseEntity<List<getDeliveryManagerResponseDto>> searchDeliveryManager(
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) DeliveryType type,
            @RequestParam(required = false) Integer sequence,
            @RequestParam(required = false) DeliveryStatus status) {
        return ResponseEntity.ok(userService.getDeliveryManagersByHubAndSettings(hubId, type, sequence, status));
    }

}