package dutchiepay.backend.domain.community.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportPostRequestDto {
    private Long postId;
    private String category;
    private Long writerId;
    private String reason;
}
