package dutchiepay.backend.domain.notice.service;

import dutchiepay.backend.domain.community.service.CommunityUtilService;
import dutchiepay.backend.domain.notice.dto.GetNoticeListResponseDto;
import dutchiepay.backend.domain.notice.repository.NoticeRepository;
import dutchiepay.backend.entity.Comment;
import dutchiepay.backend.entity.Notice;
import dutchiepay.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NoticeUtilService {
    private final NoticeRepository noticeRepository;
    private final CommunityUtilService communityUtilService;

    public List<GetNoticeListResponseDto> findRecentNotices(User user) {
        return noticeRepository.findRecentNotices(user);
    }

    public Notice createCommentNotice(String writer, Comment comment) {
        if (comment.getParentId() == null) {
            if (validatePostAuthor(writer, comment)) return null;
            return createPostCommentNotice(writer, comment);
        } else {
            if (validateCommentAuthor(writer, comment)) return null;
            return createReplyCommentNotice(writer, comment);
        }
    }

    private boolean validatePostAuthor(String writer, Comment comment) {
        return comment.getFree().getUser().getNickname().equals(writer);
    }

    private boolean validateCommentAuthor(String writer, Comment comment) {
        return comment.getUser().getNickname().equals(writer);
    }

    private Notice createPostCommentNotice(String writer, Comment comment) {
        return noticeRepository.save(Notice.builder()
                .user(comment.getFree().getUser())
                .type("comment")
                .origin(comment.getFree().getTitle())
                .content(comment.getContents())
                .originId(comment.getFree().getFreeId())
                .commentId(comment.getCommentId())
                .writer(writer)
                .isRead(false)
                .build());
    }

    private Notice createReplyCommentNotice(String writer, Comment comment) {
        Comment c = communityUtilService.findCommentById(comment.getParentId());
        return noticeRepository.save(Notice.builder()
                .user(c.getUser())
                .type("reply")
                .origin(c.getContents())
                .content(comment.getContents())
                .originId(c.getCommentId())
                .commentId(comment.getParentId())
                .writer(writer)
                .isRead(false)
                .build());
    }

    public boolean existUnreadNotification(User user, LocalDateTime time) {
        return noticeRepository.existUnreadNotification(user, time);
    }
}
