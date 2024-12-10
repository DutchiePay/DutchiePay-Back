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
    INVALID_SHARE(HttpStatus.BAD_REQUEST, "유효하지 않은 게시글입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),
    OVER_TITLE_LENGTH(HttpStatus.BAD_REQUEST, "제목은 60자 이하로 입력해주세요."),
    UNMATCHED_WRITER(HttpStatus.BAD_REQUEST, "작성자가 일치하지 않습니다."),
    INSUFFICIENT_LENGTH(HttpStatus.BAD_REQUEST, "최소 글자수를 충족하지 않았습니다."),
    ILLEGAL_FILTER(HttpStatus.BAD_REQUEST, "필터 값이 유효하지 않습니다."),
    ILLEGAL_TYPE(HttpStatus.BAD_REQUEST, "타입 값이 유효하지 않습니다."),
    /**
     * 404 Not Found
     */
    CANNOT_FOUND_POST(HttpStatus.NOT_FOUND, "자유 게시글을 찾을 수 없습니다."),
    CANNOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "원 댓글을 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
