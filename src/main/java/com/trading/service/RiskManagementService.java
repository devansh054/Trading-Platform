package com.trading.service;

import com.trading.domain.Order;
import com.trading.domain.OrderSide;
import com.trading.domain.OrderStatus;
import com.trading.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
public class RiskManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskManagementService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Value("${trading.risk-management.max-position-size:10000}")
    private BigDecimal maxPositionSize;
    
    @Value("${trading.risk-management.max-order-value:1000000}")
    private BigDecimal maxOrderValue;
    
    @Value("${trading.risk-management.restricted-symbols:}")
    private Set<String> restrictedSymbols = new HashSet<>();
    
    public RiskCheckResult validateOrder(Order order) {
        RiskCheckResult result = new RiskCheckResult();
        
        try {
            // Check restricted symbols
            if (isRestrictedSymbol(order.getSymbol())) {
                result.setValid(false);
                result.setReason("Symbol " + order.getSymbol() + " is restricted");
                return result;
            }
            
            // Check order value limit
            if (order.getPrice() != null) {
                BigDecimal orderValue = order.getQuantity().multiply(order.getPrice());
                if (orderValue.compareTo(maxOrderValue) > 0) {
                    result.setValid(false);
                    result.setReason("Order value " + orderValue + " exceeds maximum allowed " + maxOrderValue);
                    return result;
                }
            }
            
            // Check position limits
            if (!checkPositionLimits(order)) {
                result.setValid(false);
                result.setReason("Position limit exceeded for symbol " + order.getSymbol());
                return result;
            }
            
            result.setValid(true);
            result.setReason("Order passed risk checks");
            
        } catch (Exception e) {
            logger.error("Error during risk validation for order: {}", order.getOrderId(), e);
            result.setValid(false);
            result.setReason("Risk validation error: " + e.getMessage());
        }
        
        return result;
    }
    
    private boolean isRestrictedSymbol(String symbol) {
        return restrictedSymbols.contains(symbol.toUpperCase());
    }
    
    private boolean checkPositionLimits(Order order) {
        String symbol = order.getSymbol();
        String accountId = order.getAccountId();
        
        // Get current position for this symbol and account
        BigDecimal currentPosition = getCurrentPosition(symbol, accountId);
        
        // Calculate potential new position
        BigDecimal newPosition;
        if (order.getSide() == OrderSide.BUY) {
            newPosition = currentPosition.add(order.getQuantity());
        } else {
            newPosition = currentPosition.subtract(order.getQuantity());
        }
        
        // Check if new position exceeds limits
        if (newPosition.abs().compareTo(maxPositionSize) > 0) {
            logger.warn("Position limit exceeded for account {} symbol {}: current={}, new={}, limit={}", 
                       accountId, symbol, currentPosition, newPosition, maxPositionSize);
            return false;
        }
        
        return true;
    }
    
    private BigDecimal getCurrentPosition(String symbol, String accountId) {
        // Get all active orders for this symbol and account
        List<Order> activeOrders = orderRepository.findBySymbolAndAccountIdAndStatusIn(
            symbol, accountId, List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED));
        
        BigDecimal position = BigDecimal.ZERO;
        
        for (Order order : activeOrders) {
            BigDecimal orderPosition;
            if (order.getSide() == OrderSide.BUY) {
                orderPosition = order.getRemainingQuantity();
            } else {
                orderPosition = order.getRemainingQuantity().negate();
            }
            position = position.add(orderPosition);
        }
        
        return position;
    }
    
    public boolean isSymbolRestricted(String symbol) {
        return isRestrictedSymbol(symbol);
    }
    
    public BigDecimal getMaxPositionSize() {
        return maxPositionSize;
    }
    
    public BigDecimal getMaxOrderValue() {
        return maxOrderValue;
    }
    
    public Set<String> getRestrictedSymbols() {
        return new HashSet<>(restrictedSymbols);
    }
    
    public static class RiskCheckResult {
        private boolean valid;
        private String reason;
        
        public RiskCheckResult() {}
        
        public RiskCheckResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
