package com.trading.dto;

import java.math.BigDecimal;

import com.trading.domain.OrderSide;
import com.trading.domain.OrderType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderRequest {
    
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotNull(message = "Order side is required")
    private OrderSide side;
    
    @NotNull(message = "Order type is required")
    private OrderType type;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    // Price is required only for LIMIT orders
    private BigDecimal price;
    
    @NotBlank(message = "Account ID is required")
    private String accountId;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(String symbol, OrderSide side, OrderType type, 
                       BigDecimal quantity, BigDecimal price, String accountId) {
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.accountId = accountId;
    }
    
    // Getters and Setters
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
    
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    
    // Validation method
    public boolean isValid() {
        // For LIMIT orders, price is required and must be positive
        if (type == OrderType.LIMIT) {
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        }
        return true;
    }
}
