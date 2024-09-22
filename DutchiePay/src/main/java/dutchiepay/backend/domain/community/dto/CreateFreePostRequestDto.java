package dutchiepay.backend.domain.community.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateFreePostRequestDto {
    private String title;
    private String content;
    private String category;
    private String[] images;
}
