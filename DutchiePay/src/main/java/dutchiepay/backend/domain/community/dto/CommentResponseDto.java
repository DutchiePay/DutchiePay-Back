package dutchiepay.backend.domain.community.dto;
import dutchiepay.backend.entity.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponseDto {
    private List<CommentDetail> comments;
    private Long cursor;

    @Getter
    @Builder
    public static class CommentDetail {
        private Long commentId;
        private String nickname;
        private String profileImg;
        private String content;
        private LocalDateTime createdAt;
        private Boolean isModified;
        private String userState;

        public static CommentDetail toDto(Comment comment) {
            return CommentDetail.builder()
                    .commentId(comment.getCommentId())
                    .nickname(comment.getUser().getNickname())
                    .profileImg(comment.getUser().getProfileImg())
                    .content(comment.getContents())
                    .createdAt(comment.getCreatedAt())
                    .isModified(comment.getUpdatedAt() == null)
                    .userState(comment.getUser().getState() == 0? "회원" : "탈퇴")
                    .build();
        }
    }
    public static CommentResponseDto toDto(List<CommentDetail> comments, Long cursor) {
        return CommentResponseDto.builder().comments(comments).cursor(cursor).build();
    }
}