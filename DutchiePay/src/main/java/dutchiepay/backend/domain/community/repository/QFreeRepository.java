package dutchiepay.backend.domain.community.repository;

import com.querydsl.core.Tuple;
import dutchiepay.backend.domain.community.dto.FreeListResponseDto;
import dutchiepay.backend.domain.community.dto.HotAndRecommendsResponseDto;
import dutchiepay.backend.entity.Free;

import java.util.List;

public interface QFreeRepository {

    FreeListResponseDto getFreeLists(String category, String filter, int limit, Long cursor);
    Tuple getFreePost(Long freeId);

    List<HotAndRecommendsResponseDto.Posts> getHotPosts();

    List<HotAndRecommendsResponseDto.Posts> getRecommendsPosts(String category);
}
