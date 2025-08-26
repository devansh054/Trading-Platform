package com.trading.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trading.domain.Trade;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    List<Trade> findBySymbol(String symbol);
    
    List<Trade> findBySymbolAndExecutedAtBetween(String symbol, LocalDateTime start, LocalDateTime end);
    
    List<Trade> findByBuyAccountIdOrSellAccountId(String buyAccountId, String sellAccountId);
    
    @Query("SELECT t FROM Trade t WHERE t.executedAt >= :since ORDER BY t.executedAt DESC")
    List<Trade> findRecentTrades(@Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(t.totalValue) FROM Trade t WHERE t.symbol = :symbol AND t.executedAt >= :since")
    BigDecimal getTotalVolumeBySymbol(@Param("symbol") String symbol, @Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(t.price) FROM Trade t WHERE t.symbol = :symbol AND t.executedAt >= :since")
    BigDecimal getAveragePriceBySymbol(@Param("symbol") String symbol, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM Trade t WHERE t.executedAt >= :since")
    long getTradeCountSince(@Param("since") LocalDateTime since);
}
