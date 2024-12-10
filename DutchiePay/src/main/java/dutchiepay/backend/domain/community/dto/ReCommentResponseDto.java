package dutchiepay.backend.domain.community.dto;

import dutchiepay.backend.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReCommentResponseDto {

    private Long commentId;
    private String mentionedNickname;
    private String nickname;
    private String profileImg;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isModified;
    private String userState;
    private String mentionedUserState;

    public static ReCommentResponseDto toDto(Comment comment, Comment mentionedComment) {
        return ReCommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .mentionedNickname(mentionedComment.getUser().getNickname())
                .nickname(comment.getUser().getNickname())
                .profileImg(comment.getUser().getProfileImg())
                .content(comment.getContents())
                .createdAt(comment.getCreatedAt())
                .isModified(comment.getUpdatedAt() == null)
                .userState(comment.getUser().getState() == 0 ? "회원" : "탈퇴")
                .mentionedUserState(mentionedComment.getUser().getState() == 0 ? "회원" : "탈퇴")
                .build();
    }
}