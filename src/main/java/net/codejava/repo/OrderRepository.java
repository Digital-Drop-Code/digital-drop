package net.codejava.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.codejava.entity.Order;
import net.codejava.entity.OrderKey;
import net.codejava.entity.User;
import net.codejava.model.GraphRow;

public interface OrderRepository extends JpaRepository<Order, OrderKey> {

	List<Order> findAllByUser(User user);
	
	Page<Order> findByIdOrderNoContainingIgnoreCaseAndUser(String keyword, User user, Pageable pageable);


	@Query("SELECT new net.codejava.model.GraphRow(FUNCTION('MONTHNAME', o.dateCreated),count(o),0.0) "
			+ " FROM Order o WHERE o.user = :user "
			+ " AND ((:fromDate is null AND :toDate is null) OR (o.dateCreated >= :fromDate AND o.dateCreated <= :toDate))"
			+ "GROUP BY FUNCTION('MONTHNAME', o.dateCreated),EXTRACT(MONTH FROM o.dateCreated) ORDER BY EXTRACT(MONTH FROM o.dateCreated) ")
	List<GraphRow> countByUserAndMonth(@Param("user") User user,@Param("fromDate")Date fromDate ,@Param("toDate") Date toDate);
	
	@Query("SELECT new net.codejava.model.GraphRow(o.city ,count(o)) "
			+ " FROM Order o WHERE o.user = :user"
			+ " AND ((:fromDate is null AND :toDate is null) OR (o.dateCreated >= :fromDate AND o.dateCreated <= :toDate))"
			+ " GROUP BY o.city ")
	List<GraphRow> countByUserAndCity(@Param("user") User user,@Param("fromDate")Date fromDate ,@Param("toDate") Date toDate);
	
	@Query("SELECT new net.codejava.model.GraphRow(FUNCTION('MONTHNAME', o.dateCreated),0,sum(o.total)) "
			+ " FROM Order o WHERE o.user = :user"
			+ " AND ((:fromDate is null AND :toDate is null) OR (o.dateCreated >= :fromDate AND o.dateCreated <= :toDate))"
			+ " GROUP BY FUNCTION('MONTHNAME', o.dateCreated),EXTRACT(MONTH FROM o.dateCreated) ORDER BY EXTRACT(MONTH FROM o.dateCreated) ")
	List<GraphRow> amountByUserAndMonth(@Param("user") User user,@Param("fromDate")Date fromDate ,@Param("toDate") Date toDate);
	
	Long countByUser(User user);

	Long countByUserAndTrackingNoNotNull(User user);

	Long countByUserAndStatus(User user, String string);
	
	Long countByUserAndDateCreatedBetween(User user,Date fromDate , Date toDate);

	Long countByUserAndTrackingNoNotNullAndDateCreatedBetween(User user,Date fromDate , Date toDate);

	Long countByUserAndStatusAndDateCreatedBetween(User user, String string,Date fromDate , Date toDate);

	@Query("SELECT o FROM Order o WHERE o.user = :user and o.settledDate is not null ORDER BY o.settledDate")
	List<Order> findByUserOrderByDateCreated(@Param("user") User user);
	
	@Query("SELECT o FROM Order o WHERE o.user = :user and o.settledDate is not null AND o.settledDate >= :fromDate AND o.settledDate <= :toDate ORDER BY o.settledDate")
	List<Order> findByUserAndDateCreatedBetweenOrderByDateCreated(@Param("user") User user,@Param("fromDate")Date fromDate ,@Param("toDate") Date toDate);

	
	Order findFirstByIdOrderNoAndUser(String orderNo, User user);

	@Query("SELECT o FROM Order o WHERE o.user = :user and o.trackingNo is null and (o.status = 'Unpaid' OR o.status is null)"
			+ " AND ((:fromDate is null AND :toDate is null) OR (o.dateCreated >= :fromDate AND o.dateCreated <= :toDate))"
			+ " ORDER BY o.dateCreated")

	List<Order> getUnprocessOrder(@Param("user") User user,@Param("fromDate")Date fromDate ,@Param("toDate") Date toDate);

	@Query("SELECT o FROM Order o WHERE o.user = :user "
			+ " AND ((:fromDate is null AND :toDate is null) OR (o.dateCreated >= :fromDate AND o.dateCreated <= :toDate))"
			+ " ORDER BY o.dateCreated")

	List<Order> getAllOrder(@Param("user") User user,@Param("fromDate")Date fromDate ,@Param("toDate") Date toDate);


	@Query("SELECT o FROM Order o WHERE o.user = :user and o.trackingNo is not null and o.shipmentDate is not null "
			+ " AND ((:fromDate is null AND :toDate is null) OR (o.shipmentDate >= :fromDate AND o.shipmentDate <= :toDate))"
			+ " ORDER BY o.shipmentDate")

	List<Order> getAllTrackedOrder(@Param("user") User user,@Param("fromDate")Date fromDate ,@Param("toDate") Date toDate);

}
