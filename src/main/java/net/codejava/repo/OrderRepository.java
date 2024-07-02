package net.codejava.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import net.codejava.entity.Order;
import net.codejava.entity.OrderKey;
import net.codejava.entity.User;
import net.codejava.model.GraphRow;

public interface OrderRepository extends JpaRepository<Order, OrderKey> {

	List<Order> findAllByUser(User user);
	
	Page<Order> findByIdOrderNoContainingIgnoreCaseAndUser(String keyword, User user, Pageable pageable);


	@Query("SELECT new net.codejava.model.GraphRow(FUNCTION('MONTHNAME', o.dateCreated),count(o),0.0) "
			+ " FROM Order o WHERE o.user = :user GROUP BY FUNCTION('MONTHNAME', o.dateCreated),EXTRACT(MONTH FROM o.dateCreated) ORDER BY EXTRACT(MONTH FROM o.dateCreated) ")
	List<GraphRow> countByUserAndMonth(User user);
	
	@Query("SELECT new net.codejava.model.GraphRow(o.city ,count(o)) "
			+ " FROM Order o WHERE o.user = :user GROUP BY o.city ")
	List<GraphRow> countByUserAndCity(User user);
	
	@Query("SELECT new net.codejava.model.GraphRow(FUNCTION('MONTHNAME', o.dateCreated),0,sum(o.total)) "
			+ " FROM Order o WHERE o.user = :user GROUP BY FUNCTION('MONTHNAME', o.dateCreated),EXTRACT(MONTH FROM o.dateCreated) ORDER BY EXTRACT(MONTH FROM o.dateCreated) ")
	List<GraphRow> amountByUserAndMonth(User user);
	
	Long countByUser(User user);

	Long countByUserAndTrackingNoNotNull(User user);

	Order findFirstByIdOrderNoAndUser(String orderNo, User user);

	Long countByUserAndStatus(User user, String string);

	List<Order> findByUserOrderByDateCreated(User user);

}
