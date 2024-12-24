package dutchiepay.backend.domain.community.dto;

import com.querydsl.core.Tuple;
import dutchiepay.backend.entity.Free;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static dutchiepay.backend.entity.QFree.free;
import static dutchiepay.backend.entity.QUser.user;

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
        private Long commentCount;

        public static Posts toDto(Tuple result, Long count) {
            return Posts.builder()
                    .freeId(result.get(free.freeId))
                    .writerProfileImg(result.get(user.profileImg))
                    .writer(result.get(user.nickname))
                    .title(result.get(free.title))
                    .commentCount(count)
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
