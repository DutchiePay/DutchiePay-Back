package dutchiepay.backend.domain.community.exception;

import lombok.Getter;

@Getter
public class CommunityException extends RuntimeException{

    private final CommunityErrorCode communityErrorCode;

    public CommunityException(CommunityErrorCode communityErrorCode) {
        super(communityErrorCode.getMessage());
        this.communityErrorCode = communityErrorCode;
    }

    @Override
    public String toString() {
        return String.format("CommerceErrorException(code=%s, message=%s)",
                communityErrorCode.name(), communityErrorCode.getMessage());
    }
}
