package dutchiepay.backend.global.exception;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorMessage {
    private String message;

    public static ErrorMessage of(String message) {
        return ErrorMessage.builder()
                .message(message)
                .build();
    }
}
