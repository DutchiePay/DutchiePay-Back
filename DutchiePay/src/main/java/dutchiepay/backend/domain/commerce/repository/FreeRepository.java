package dutchiepay.backend.domain.commerce.repository;

import dutchiepay.backend.entity.Free;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FreeRepository extends JpaRepository<Free, Long> {
}
