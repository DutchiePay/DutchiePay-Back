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

@Service
@RequiredArgsConstructor
public class CommunityUtilService {
    private final FreeRepository freeRepository;
    private final CommentRepository commentRepository;
    private final PostHitService postHitService;

    // 게시글 Id로 Free 객체 조회
    public Free findFreeById(Long freeId) {
        return freeRepository.findById(freeId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_POST));
    }

    // QFreeRepository에서 게시글 단건 조회
    public FreePostResponseDto getFreePost(User user, Long freeId) {
        FreePostResponseDto result = qFreeRepository.getFreePost(freeId);

        // 문제 없을 경우 조회수 증가
        postHitService.increaseHitCount(user, "free", freeId);

        return result;
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

    // 게시글을 찾고 작성자를 검증
    public Free validatePostAndWriter(User user, Long freeId) {
        Free free = findFreeById(freeId);
        validatePostWriter(user, free);
        return free;
    }

    // 댓글 길이 검증
    public static void validateCommentLength(String comment) {
        if (comment.length() < 2) throw new CommunityException(CommunityErrorCode.INSUFFICIENT_LENGTH);
        if (comment.length() > 800) throw new CommunityException(CommunityErrorCode.OVER_CONTENT_LENGTH);
    }

    // 댓글 작성 시 Comment 객체 생성 후 저장
    public CommentCreateResponseDto createComment(User user, CommentCreateRequestDto commentRequestDto) {
        return CommentCreateResponseDto.toDto(
                commentRepository.save(
                        Comment.builder()
                                .free(findFreeById(commentRequestDto.getFreeId())).contents(commentRequestDto.getContent())
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

    // 댓글을 찾고 작성자를 검증
    public Comment validateCommentAndWriter(User user, Long commentId) {
        Comment comment = findCommentById(commentId);
        validateCommentWriter(user, comment);
        return comment;
    }
}
