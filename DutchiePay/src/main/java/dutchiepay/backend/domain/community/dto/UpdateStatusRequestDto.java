package dutchiepay.backend.domain.community.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateStatusRequestDto {
    private String postId;
    private String category;
    private String status;
}
