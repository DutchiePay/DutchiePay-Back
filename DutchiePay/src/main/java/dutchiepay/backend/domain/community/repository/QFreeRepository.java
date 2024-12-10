package dutchiepay.backend.domain.community.repository;

import dutchiepay.backend.domain.community.dto.*;

import java.util.List;

public interface QFreeRepository {

    FreeListResponseDto getFreeLists(String category, String filter, int limit, Long cursor);
    FreePostResponseDto getFreePost(Long freeId);

    List<HotAndRecommendsResponseDto.Posts> getHotPosts();

    List<HotAndRecommendsResponseDto.Posts> getRecommendsPosts(String category);

    CommentResponseDto getComments(Long freeId, Long cursor, int limit);

    List<ReCommentResponseDto> getReComments(Long commentId, String type);
}
