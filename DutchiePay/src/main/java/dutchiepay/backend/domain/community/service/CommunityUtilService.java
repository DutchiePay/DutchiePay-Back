package dutchiepay.backend.domain.community.service;

import dutchiepay.backend.domain.commerce.repository.CommentRepository;
import dutchiepay.backend.domain.commerce.repository.FreeRepository;
import dutchiepay.backend.domain.community.dto.*;
import dutchiepay.backend.domain.community.exception.CommunityErrorCode;
import dutchiepay.backend.domain.community.exception.CommunityException;
import dutchiepay.backend.domain.community.repository.QFreeRepositoryImpl;
import dutchiepay.backend.entity.Comment;
import dutchiepay.backend.entity.Free;
import dutchiepay.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityUtilService {
    private final QFreeRepositoryImpl qFreeRepository;
    private final FreeRepository freeRepository;
    private final CommentRepository commentRepository;

    // QFreeRepository에서 게시글 목록 조회
    public FreeListResponseDto getFreeLists(String category, String filter, int limit, Long cursor) {
        return qFreeRepository.getFreeLists(category, filter, limit, cursor);
    }

    // 게시글 Id로 Free 객체 조회
    public Free findFreeById(Long freeId) {
        return freeRepository.findById(freeId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_POST));
    }

    // QFreeRepository에서 게시글 단건 조회
    public FreePostResponseDto getFreePost(Long freeId) {
        return qFreeRepository.getFreePost(freeId);
    }

    // 게시글의 길이를 검증한 후 저장
    public String validatePostLength(String content) {
        String description = content.replaceAll("<[^>]*>", "");
        if (description.length() < 2)
            throw new CommunityException(CommunityErrorCode.INSUFFICIENT_LENGTH);
        return description;
    }

    // 게시글 작성 시 Free 객체 생성 후 저장
    public Free saveFree(User user, CreateFreeRequestDto createFreeRequestDto, String description) {
        return freeRepository.save(Free.builder()
                .user(user)
                .title(createFreeRequestDto.getTitle())
                .contents(createFreeRequestDto.getContent())
                .category(createFreeRequestDto.getCategory())
                .postImg(createFreeRequestDto.getThumbnail())
                .description(description.substring(0, Math.min(description.length(), 100)))
                .build());
    }

    // 게시글 작성자를 검증
    public static void validatePostWriter(User user, Free free) {
        if (!free.getUser().equals(user)) throw new CommunityException(CommunityErrorCode.UNMATCHED_WRITER);
    }

    // QFreeRepository에서 인기, 추천 게시글을 찾음
    public HotAndRecommendsResponseDto getHotAndRecommendPosts(String category) {
        return HotAndRecommendsResponseDto.toDto(qFreeRepository.getHotPosts(),
                qFreeRepository.getRecommendsPosts(category));
    }

    // 원댓 목록 조회
    public CommentResponseDto getComments(Long freeId, Long cursor, int limit) {
        return qFreeRepository.getComments(freeId, cursor, limit);
    }

    // 답댓 목록 조회
    public List<ReCommentResponseDto> getReComments(Long commentId, String type) {
        return qFreeRepository.getReComments(commentId, type);
    }

    // 댓글 작성 시 Comment 객체 생성 후 저장
    public CommentCreateResponseDto createComment(User user, CommentCreateRequestDto commentRequestDto, Free free) {
        return CommentCreateResponseDto.toDto(
                commentRepository.save(
                        Comment.builder()
                                .free(free).contents(commentRequestDto.getContent())
                                .user(user).mentionedId(commentRequestDto.getMentionedId())
                                .parentId(commentRequestDto.getRootCommentId()).build()));
    }

    // commentId로 Comment 객체를 찾음
    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_COMMENT));
    }

    // 댓글 작성자를 검증
    public static void validateCommentWriter(User user, Comment comment) {
        if (!comment.getUser().getUserId().equals(user.getUserId())) throw new CommunityException(CommunityErrorCode.UNMATCHED_WRITER);
    }
}
