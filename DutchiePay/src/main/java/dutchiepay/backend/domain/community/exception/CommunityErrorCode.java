package dutchiepay.backend.domain.community.exception;

import dutchiepay.backend.global.exception.StatusCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CommunityErrorCode implements StatusCode {

    /**
     * 400 Bad Request
     */
    UNMATCHED_WRITER(HttpStatus.BAD_REQUEST, "작성자가 일치하지 않습니다."),
    INSUFFICIENT_LENGTH(HttpStatus.BAD_REQUEST, "최소 글자수를 충족하지 않았습니다."),
    ILLEGAL_FILTER(HttpStatus.BAD_REQUEST, "필터 값이 유효하지 않습니다."),
    /**
     * 404 Not Found
     */
    CANNOT_FOUND_POST(HttpStatus.NOT_FOUND, "자유 게시글을 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
