package dutchiepay.backend.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PurchaseResponseDto {
    private Long writerId;
    private String writer;
    private String writerProfileImage;
    private String title;
    private String category;
    private String contents;
    private String goods;
    private Integer price;
    private String meetingPlace;
    private String latitude;
    private String longitude;
    private String state;
    private LocalDateTime createdAt;
    private Integer hits;

}
