package dutchiepay.backend.domain.community.dto;

import dutchiepay.backend.entity.Free;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FreeListResponseDto {

    List<FreeList> posts;
    Long cursor;

    @Getter
    @Builder
    public static class FreeList {
        private Long freeId;
        private String title;
        private String description;
        private String writer;
        private String createdAt;
        private String category;
        private Long commentsCount;
        private String thumbnail;
        private String writerProfileImg;

        public static FreeList toDto(Free free, String createdAt, Long count) {
            return FreeList.builder()
                    .freeId(free.getFreeId())
                    .title(free.getTitle())
                    .description(free.getDescription())
                    .writer(free.getUser().getNickname())
                    .createdAt(createdAt)
                    .category(free.getCategory())
                    .commentsCount(count)
                    .thumbnail(free.getPostImg())
                    .writerProfileImg(free.getUser().getProfileImg())
                    .build();
        }
    }

    public static FreeListResponseDto toDto(List<FreeList> list, Long cursor) {
        return FreeListResponseDto.builder()
                .posts(list)
                .cursor(cursor)
                .build();
    }

}
