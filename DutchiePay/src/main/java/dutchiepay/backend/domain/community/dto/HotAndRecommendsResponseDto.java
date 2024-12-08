package dutchiepay.backend.domain.community.dto;

import dutchiepay.backend.entity.Free;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HotAndRecommendsResponseDto {

    List<Posts> hot;
    List<Posts> recommends;

    @Getter
    @Builder
    public static class Posts {
        private Long freeId;
        private String writerProfileImg;
        private String writer;
        private String title;
        private Integer commentCount;

        public static Posts toDto(Free free, Long count) {
            return Posts.builder()
                    .freeId(free.getFreeId())
                    .writerProfileImg(free.getUser().getProfileImg())
                    .writer(free.getUser().getNickname())
                    .title(free.getTitle())
                    .commentCount(Math.toIntExact(count))
                    .build();
        }
    }

    public static HotAndRecommendsResponseDto toDto(List<Posts> hot, List<Posts> recommends) {
        return HotAndRecommendsResponseDto.builder()
                .hot(hot)
                .recommends(recommends)
                .build();
    }
}
