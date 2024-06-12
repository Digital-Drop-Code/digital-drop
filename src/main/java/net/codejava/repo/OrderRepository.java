package net.codejava.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.codejava.entity.Order;
import net.codejava.entity.OrderKey;
import net.codejava.entity.User;

public interface OrderRepository extends JpaRepository<Order, OrderKey> {

	List<Order> findAllByUser(User user);
	
	Page<Order> findByIdOrderNoContainingIgnoreCaseAndUser(String keyword, User user, Pageable pageable);


	Long countByUser(User user);

	Long countByUserAndTrackingNoNotNull(User user);

	Order findFirstByIdOrderNoAndUser(String orderNo, User user);

	Long countByUserAndStatus(User user, String string);

	List<Order> findByUserOrderByDateCreated(User user);

}
