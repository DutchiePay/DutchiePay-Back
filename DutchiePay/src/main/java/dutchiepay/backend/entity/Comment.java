package dutchiepay.backend.entity;

import dutchiepay.backend.global.config.Auditing;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Table(name = "Comment")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends Auditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "free_post_id")
    private FreePost freePostId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    // 상위 댓글 Id
    private Long parentId;

    // 언급한 댓글 Id
    private Long mentionedId;

    // 댓글 내용
    @Column(nullable = false, length = 800)
    private String contents;
}
