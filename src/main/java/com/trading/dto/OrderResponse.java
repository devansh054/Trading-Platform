package com.trading.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trading.domain.Order;
import com.trading.domain.OrderSide;
import com.trading.domain.OrderStatus;
import com.trading.domain.OrderType;

public class OrderResponse {
    
    private String orderId;
    private String symbol;
    private OrderSide side;
    private OrderType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal filledQuantity;
    private BigDecimal remainingQuantity;
    private OrderStatus status;
    private String accountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime filledAt;
    private String reason;
    
    public OrderResponse() {}
    
    public OrderResponse(Order order) {
        this.orderId = order.getOrderId();
        this.symbol = order.getSymbol();
        this.side = order.getSide();
        this.type = order.getType();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.filledQuantity = order.getFilledQuantity();
        this.remainingQuantity = order.getRemainingQuantity();
        this.status = order.getStatus();
        this.accountId = order.getAccountId();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        this.filledAt = order.getFilledAt();
        this.reason = order.getReason();
    }
    
    // Getters and Setters
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getFilledAt() { return filledAt; }
    public void setFilledAt(LocalDateTime filledAt) { this.filledAt = filledAt; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
