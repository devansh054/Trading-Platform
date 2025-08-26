package com.trading.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trading.domain.Order;
import com.trading.domain.OrderSide;
import com.trading.domain.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderId(String orderId);
    
    List<Order> findBySymbolAndStatus(String symbol, OrderStatus status);
    
    List<Order> findBySymbolAndSideAndStatus(String symbol, OrderSide side, OrderStatus status);
    
    List<Order> findByAccountIdAndStatus(String accountId, OrderStatus status);
    
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime before);
    
    @Query("SELECT o FROM Order o WHERE o.symbol = :symbol AND o.side = :side AND o.status = :status AND o.price >= :minPrice AND o.price <= :maxPrice ORDER BY o.price ASC")
    List<Order> findOrdersByPriceRange(@Param("symbol") String symbol, 
                                      @Param("side") OrderSide side, 
                                      @Param("status") OrderStatus status,
                                      @Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.symbol = :symbol AND o.accountId = :accountId AND o.status IN (:statuses)")
    long countActiveOrdersBySymbolAndAccount(@Param("symbol") String symbol, 
                                           @Param("accountId") String accountId,
                                           @Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT SUM(o.remainingQuantity * o.price) FROM Order o WHERE o.accountId = :accountId AND o.status IN (:statuses)")
    BigDecimal getTotalOrderValueByAccount(@Param("accountId") String accountId,
                                         @Param("statuses") List<OrderStatus> statuses);
    
    List<Order> findBySymbolAndAccountIdAndStatusIn(String symbol, String accountId, List<OrderStatus> statuses);
    
    List<Order> findByStatus(OrderStatus status);
    
    boolean existsByOrderId(String orderId);
    
    void deleteByOrderId(String orderId);
    
    List<Order> findByUserUsername(String username);
}
