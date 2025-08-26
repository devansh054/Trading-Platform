package com.trading.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import com.trading.entity.User;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderId;
    
    @Column(nullable = false)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;
    
    @Column(nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.01")
    private BigDecimal quantity;
    
    @Column(precision = 19, scale = 4)
    @DecimalMin(value = "0.01")
    private BigDecimal price;
    
    @Column(nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal filledQuantity = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 19, scale = 4)
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal remainingQuantity = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false)
    private String accountId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private com.trading.entity.User user;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime filledAt;
    
    @Column
    private String reason;
    
    // Constructors
    public Order() {}
    
    public Order(String orderId, String symbol, OrderSide side, OrderType type, 
                 BigDecimal quantity, BigDecimal price, String accountId) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.filledQuantity = BigDecimal.ZERO;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.PENDING;
        this.accountId = accountId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }
    
    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(BigDecimal filledQuantity) { this.filledQuantity = filledQuantity; }
    
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(BigDecimal remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    
    public com.trading.entity.User getUser() { return user; }
    public void setUser(com.trading.entity.User user) { this.user = user; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getFilledAt() { return filledAt; }
    public void setFilledAt(LocalDateTime filledAt) { this.filledAt = filledAt; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    // Business methods
    public boolean isFullyFilled() {
        return filledQuantity.compareTo(quantity) >= 0;
    }
    
    public boolean isPartiallyFilled() {
        return filledQuantity.compareTo(BigDecimal.ZERO) > 0 && !isFullyFilled();
    }
    
    public void updateFilledQuantity(BigDecimal fillQuantity) {
        this.filledQuantity = this.filledQuantity.add(fillQuantity);
        this.remainingQuantity = this.quantity.subtract(this.filledQuantity);
        this.updatedAt = LocalDateTime.now();
        
        if (isFullyFilled()) {
            this.status = OrderStatus.FILLED;
            this.filledAt = LocalDateTime.now();
        } else if (isPartiallyFilled()) {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
