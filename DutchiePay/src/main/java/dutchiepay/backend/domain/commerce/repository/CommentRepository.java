package dutchiepay.backend.domain.commerce.repository;

import dutchiepay.backend.entity.Comment;
import dutchiepay.backend.entity.Free;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    int countCommentByFree(Free free);
}
