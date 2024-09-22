package dutchiepay.backend.domain.community.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateSharePostRequestDto {
    private String title;
    private String content;
    private Integer price;
    private String meetingPlace;
    private String latitude;
    private String longitude;
    private String goods;
    private String[] images;
}
