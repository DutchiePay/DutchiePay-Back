package dutchiepay.backend.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FreePostResponseDto {

    private Long writerId;
    private String writer;
    private String writerProfileImage;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String category;
    private Long commentsCount;
    private Integer hits;

}
