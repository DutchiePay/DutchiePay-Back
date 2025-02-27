package dutchiepay.backend.domain.profile.exception;

import dutchiepay.backend.global.exception.StatusCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProfileErrorCode implements StatusCode {
    /**
     * 400 BAD_REQUEST
     */
    INVALID_ASK(HttpStatus.BAD_REQUEST, "문의 정보가 없습니다."),
    INVALID_REVIEW(HttpStatus.BAD_REQUEST, "후기 정보가 없습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."),
    INVALID_POST_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 게시글 타입입니다."),
    INVALID_USER_ORDER_REVIEW(HttpStatus.BAD_REQUEST, "자신이 구매한 상품에만 후기를 달 수 있습니다."),
    INVALID_USER_ORDER_ASK(HttpStatus.BAD_REQUEST, "자신이 구매한 상품에만 문의를 달 수 있습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "올바르지 않은 필터입니다."),

    /**
     * 401 UNAUTHORIZED
     */
    DELETE_ASK_USER_MISSMATCH(HttpStatus.UNAUTHORIZED, "본인만 문의 삭제를 할 수 있습니다."),

    /**
     * 403 FORBIDDEN
     */
    DELETE_REVIEW_USER_MISSMATCH(HttpStatus.FORBIDDEN, "본인만 후기 삭제를 할 수 있습니다."),
    UPDATE_REVIEW_USER_MISSMATCH(HttpStatus.FORBIDDEN, "본인만 후기 수정을 할 수 있습니다."),

    /**
     * 404 NOT_FOUND
     */
    NO_HISTORY_ORDER(HttpStatus.NOT_FOUND, "주문 내역이 없습니다."),
    NO_MORE_HISTORY_ORDER(HttpStatus.NOT_FOUND, "더 이상 주문 내역이 없습니다."),;

    private final HttpStatus httpStatus;
    private final String message;
}
