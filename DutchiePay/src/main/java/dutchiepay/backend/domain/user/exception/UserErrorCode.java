package dutchiepay.backend.domain.user.exception;

import dutchiepay.backend.global.Exception.StatusCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserErrorCode implements StatusCode {
    /**
     * 400 BAD_REQUEST
     */
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당하는 유저가 없습니다.");
    USER_NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다."),
    USER_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."),
    USER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다.");

    /**
     * 401 UNAUTHORIZED
     */


    /**
     * 403 FORBIDDEN
     */

    private final HttpStatus httpStatus;
    private final String message;
}
