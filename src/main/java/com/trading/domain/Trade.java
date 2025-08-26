package com.trading.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "trades")
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String tradeId;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.01")
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.01")
    private BigDecimal price;
    
    @Column(nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.01")
    private BigDecimal totalValue;
    
    @Column(nullable = false)
    private String buyOrderId;
    
    @Column(nullable = false)
    private String sellOrderId;
    
    @Column(nullable = false)
    private String buyAccountId;
    
    @Column(nullable = false)
    private String sellAccountId;
    
    @Column(nullable = false)
    private LocalDateTime executedAt;
    
    @Column
    private String executionVenue;
    
    // Constructors
    public Trade() {}
    
    public Trade(String tradeId, String symbol, BigDecimal quantity, BigDecimal price,
                 String buyOrderId, String sellOrderId, String buyAccountId, String sellAccountId) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.totalValue = quantity.multiply(price);
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyAccountId = buyAccountId;
        this.sellAccountId = sellAccountId;
        this.executedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    
    public String getBuyOrderId() { return buyOrderId; }
    public void setBuyOrderId(String buyOrderId) { this.buyOrderId = buyOrderId; }
    
    public String getSellOrderId() { return sellOrderId; }
    public void setSellOrderId(String sellOrderId) { this.sellOrderId = sellOrderId; }
    
    public String getBuyAccountId() { return buyAccountId; }
    public void setBuyAccountId(String buyAccountId) { this.buyAccountId = buyAccountId; }
    
    public String getSellAccountId() { return sellAccountId; }
    public void setSellAccountId(String sellAccountId) { this.sellAccountId = sellAccountId; }
    
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    
    public String getExecutionVenue() { return executionVenue; }
    public void setExecutionVenue(String executionVenue) { this.executionVenue = executionVenue; }
}
