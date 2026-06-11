package com.lalitha.sweets.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lalitha.sweets.model.*;

public interface OrderRepository extends JpaRepository<Order, Long> {

	Order findTopByOrderByIdDesc();

	List<Order> findByStatus(OrderStatus status);

	@Query("SELECT COUNT(o) FROM Order o")
	long countTotalOrders();

	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
	BigDecimal totalRevenue();

	@Query(value = """
            SELECT COALESCE(SUM(total_amount), 0)
            FROM orders
            WHERE order_date >= CURRENT_DATE
              AND order_date < CURRENT_DATE + INTERVAL '1 day'
            """, nativeQuery = true)
	BigDecimal todayRevenue();

	@Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
	long countByStatus(@Param("status") OrderStatus status);

	@Query(value = """
            SELECT COUNT(*)
            FROM orders
            WHERE order_date >= CURRENT_DATE
              AND order_date < CURRENT_DATE + INTERVAL '1 day'
            """, nativeQuery = true)
	long countTodayOrders();

	@Query(value = """
            SELECT
                EXTRACT(MONTH FROM order_date) AS month,
                COUNT(*) AS total_orders
            FROM orders
            GROUP BY EXTRACT(MONTH FROM order_date)
            ORDER BY month
            """, nativeQuery = true)
	List<Object[]> getMonthlyOrders();

	default List<Object[]> monthlyOrders() {
		return getMonthlyOrders();
	}

	@Query("""
            SELECT o
            FROM Order o
            LEFT JOIN FETCH o.items
            WHERE o.id = :id
            """)
	Optional<Order> findByIdWithItems(@Param("id") Long id);

	@Query(value = """
            SELECT DISTINCT o.*
            FROM orders o
            LEFT JOIN customers c ON c.id = o.customer_id
            WHERE
                CAST(o.id AS TEXT) LIKE CONCAT('%', :keyword, '%')
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY o.id DESC
            """, nativeQuery = true)
	List<Order> search(@Param("keyword") String keyword);

	List<Order> findTop5ByOrderByIdDesc();

	Page<Order> findAll(Pageable pageable);

	List<Order> findAllByOrderByIdDesc();

	Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

	List<Order> findAllByOrderByOrderDateDesc();

	@Query("""
            SELECT o
            FROM Order o
            WHERE
                (
                    :search IS NULL
                    OR str(o.id) LIKE CONCAT('%', :search, '%')
                    OR LOWER(o.customerNameSnapshot)
                       LIKE LOWER(CONCAT('%', :search, '%'))
                )
            AND
                (
                    :status IS NULL
                    OR o.status = :status
                )
            AND
                (
                    :fromDate IS NULL
                    OR o.orderDate >= :fromDate
                )
            AND
                (
                    :toDate IS NULL
                    OR o.orderDate <= :toDate
                )
            ORDER BY o.orderDate DESC
            """)
	Page<Order> findOrdersWithFilters(
			@Param("search") String search,
			@Param("status") OrderStatus status,
			@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate,
			Pageable pageable
	);
}