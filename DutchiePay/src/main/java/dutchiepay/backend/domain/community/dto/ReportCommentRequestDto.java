package dutchiepay.backend.domain.community.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportCommentRequestDto {
    private Long postId;
    private Long commentId;
    private Long writerId;
    private String reason;
}
