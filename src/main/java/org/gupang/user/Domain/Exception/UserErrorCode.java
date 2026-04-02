package org.gupang.user.Domain.Exception;

import org.gupang.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements BaseErrorCode {
    
    PENDING_APPROVAL(HttpStatus.BAD_REQUEST, "가입 승인 대기 중입니다. 관리자의 승인이 필요합니다."),
    REJECTED_USER(HttpStatus.BAD_REQUEST, "가입 승인이 거절 되었습니다. 다시 신청 부탁드립니다."),
    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "아이디는 4자 이상 10자 이하로 입력해주세요. (알파벳 소문자, 숫자만 가능)"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상 15자 이하로 입력해주세요. (알파벳 대소문자, 숫자, 특수문자 가능)");

    private final HttpStatus httpStatus;
    private final String message;

    UserErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
