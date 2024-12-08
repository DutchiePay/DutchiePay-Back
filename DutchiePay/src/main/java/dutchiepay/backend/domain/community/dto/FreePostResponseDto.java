package dutchiepay.backend.domain.community.dto;

import dutchiepay.backend.entity.Free;
import dutchiepay.backend.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FreePostResponseDto {

    private Long writerId;
    private String writer;
    private String writerProfileImage;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String category;
    private Integer commentsCount;
    private Integer hit;

    public static FreePostResponseDto toDto(User user, Free free, int count) {
        return FreePostResponseDto.builder()
                .writerId(user.getUserId())
                .writer(user.getNickname())
                .writerProfileImage(user.getProfileImg())
                .title(free.getTitle())
                .content(free.getContents())
                .createdAt(free.getCreatedAt())
                .category(free.getCategory())
                .hit(free.getHits())
                .build();

    }
}
