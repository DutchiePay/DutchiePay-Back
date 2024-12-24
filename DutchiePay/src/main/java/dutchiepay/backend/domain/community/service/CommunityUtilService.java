package dutchiepay.backend.domain.community.service;

import dutchiepay.backend.domain.commerce.repository.CommentRepository;
import dutchiepay.backend.domain.commerce.repository.FreeRepository;
import dutchiepay.backend.domain.community.dto.*;
import dutchiepay.backend.domain.community.exception.CommunityErrorCode;
import dutchiepay.backend.domain.community.exception.CommunityException;
import dutchiepay.backend.entity.Comment;
import dutchiepay.backend.entity.Free;
import dutchiepay.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommunityUtilService {
    private final FreeRepository freeRepository;
    private final CommentRepository commentRepository;

    // 게시글 Id로 Free 객체 조회
    public Free findFreeById(Long freeId) {
        return freeRepository.findByFreeIdAndDeletedAtIsNull(freeId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_POST));
    }

    // 게시글의 길이를 검증한 후 저장
    public String validatePostLength(String content) {
        String description = content.replaceAll("<[^>]*>", "").replaceAll("^\"|\"$", "");
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
                .thumbnail(createFreeRequestDto.getThumbnail())
                .images(String.join(",", createFreeRequestDto.getImages()))
                .description(description.substring(0, Math.min(description.length(), 100)))
                .build());
    }

    // 게시글 작성자를 검증
    public static void validatePostWriter(User user, Free free) {
        if (!free.getUser().getUserId().equals(user.getUserId())) throw new CommunityException(CommunityErrorCode.UNMATCHED_WRITER);
    }

    // 게시글을 찾고 작성자를 검증
    public Free validatePostAndWriter(User user, Long freeId) {
        Free free = findFreeById(freeId);
        validatePostWriter(user, free);
        return free;
    }

    // 게시글 수정
    public void updatePost(User user, UpdateFreeRequestDto updateFreeRequestDto, String description) {
        Free free = validatePostAndWriter(user, updateFreeRequestDto.getFreeId());
        free.updateFree(updateFreeRequestDto, description, String.join(",", updateFreeRequestDto.getImages()));
    }

    // 댓글 길이 검증
    public static void validateCommentLength(String comment) {
        if (comment.length() < 2) throw new CommunityException(CommunityErrorCode.INSUFFICIENT_LENGTH);
        if (comment.length() > 800) throw new CommunityException(CommunityErrorCode.OVER_CONTENT_LENGTH);
    }


    // 댓글 작성 시 Comment 객체 생성 후 저장
    public Comment createComment(User user, CommentCreateRequestDto commentRequestDto) {
        // 답글이면 검증 수행
        if (commentRequestDto.getRootCommentId() != null || commentRequestDto.getMentionedId() != null) {
            validateCommentCreateRequest(commentRequestDto);
        }
        return commentRepository.save(
                        Comment.builder()
                                .free(findFreeById(commentRequestDto.getFreeId())).contents(commentRequestDto.getContent())
                                .user(user).mentionedId(commentRequestDto.getMentionedId())
                                .parentId(commentRequestDto.getRootCommentId()).build());
    }

    private void validateCommentCreateRequest(CommentCreateRequestDto commentRequestDto) {
        // rootCommentId나 mentionedId가 null이면 exception 발생
        if (commentRequestDto.getRootCommentId() == null || commentRequestDto.getMentionedId() == null)
            throw new CommunityException(CommunityErrorCode.ILLEGAL_ARGUMENT);
        // root 댓글과 mentioned 댓글을 찾아서 deleteAt이 null인지 확인 -> 삭제된 댓글이면 exception 발생
        // root Comment나 mentioned Comment의 freeId와 현재 freeId가 다르면 exception 발생
        Comment rootComment = findCommentById(commentRequestDto.getRootCommentId());
        Comment mentionedComment = findCommentById(commentRequestDto.getMentionedId());
        if (!(rootComment.getFree().getFreeId().equals(commentRequestDto.getFreeId())) &&
                !(mentionedComment.getFree().getFreeId().equals(commentRequestDto.getFreeId())))
            throw new CommunityException(CommunityErrorCode.INVALID_POST);
    }

    // commentId로 삭제되지 않은 Comment 객체를 찾음
    public Comment findCommentById(Long commentId) {
        return commentRepository.findByCommentIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_COMMENT));
    }

    // commentId로 Comment 객체를 찾음
    public void findComment(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_COMMENT));
    }

    // 댓글 작성자를 검증
    public static void validateCommentWriter(User user, Comment comment) {
        if (!comment.getUser().getUserId().equals(user.getUserId()))
            throw new CommunityException(CommunityErrorCode.UNMATCHED_WRITER);
    }

    // 댓글을 찾고 작성자를 검증
    public Comment validateCommentAndWriter(User user, Long commentId) {
        Comment comment = findCommentById(commentId);
        validateCommentWriter(user, comment);
        return comment;
    }
}
