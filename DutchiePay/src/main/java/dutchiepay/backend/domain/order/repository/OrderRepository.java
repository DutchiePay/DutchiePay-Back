package dutchiepay.backend.domain.order.repository;

import dutchiepay.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, QOrderRepository {
}
