package com.trading.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trading.domain.IOIStatus;
import com.trading.domain.IndicationOfInterest;
import com.trading.domain.OrderSide;

@Repository
public interface IOIRepository extends JpaRepository<IndicationOfInterest, Long> {
    
    List<IndicationOfInterest> findBySymbol(String symbol);
    
    List<IndicationOfInterest> findBySymbolAndSide(String symbol, OrderSide side);
    
    List<IndicationOfInterest> findByStatus(IOIStatus status);
    
    List<IndicationOfInterest> findByBrokerId(String brokerId);
    
    List<IndicationOfInterest> findByClientId(String clientId);
    
    IndicationOfInterest findByIoiId(String ioiId);
    
    List<IndicationOfInterest> findByExpiresAtBefore(LocalDateTime before);
    
    @Query("SELECT i FROM IndicationOfInterest i WHERE i.status = :status AND i.expiresAt > :now")
    List<IndicationOfInterest> findActiveIOIs(@Param("status") IOIStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT i FROM IndicationOfInterest i WHERE i.symbol = :symbol AND i.side = :side AND i.status = :status AND i.price >= :minPrice AND i.price <= :maxPrice")
    List<IndicationOfInterest> findIOIsByPriceRange(@Param("symbol") String symbol,
                                                    @Param("side") OrderSide side,
                                                    @Param("status") IOIStatus status,
                                                    @Param("minPrice") BigDecimal minPrice,
                                                    @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT COUNT(i) FROM IndicationOfInterest i WHERE i.brokerId = :brokerId AND i.status = :status")
    long countIOIsByBrokerAndStatus(@Param("brokerId") String brokerId, @Param("status") IOIStatus status);
}
