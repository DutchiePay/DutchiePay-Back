package dutchiepay.backend.domain.community.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateFreePostRequestDto {
    private Long freePostId;
    private String title;
    private String content;
    private String category;
    private String[] images;
}
