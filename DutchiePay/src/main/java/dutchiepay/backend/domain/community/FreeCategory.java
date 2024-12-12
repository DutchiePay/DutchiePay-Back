package dutchiepay.backend.domain.community;

import dutchiepay.backend.domain.community.exception.CommunityErrorCode;
import dutchiepay.backend.domain.community.exception.CommunityException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum FreeCategory {
    info("정보"), qna("질문"), free("자유"), hobby("취미");

    private final String type;

    FreeCategory(String type) {
        this.type = type;
    }

    public static FreeCategory findByString(String type) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.ILLEGAL_TYPE));
    }
}
