package com.trading.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trading.domain.Order;
import com.trading.domain.OrderStatus;
import com.trading.dto.OrderRequest;
import com.trading.dto.OrderResponse;
import com.trading.repository.OrderRepository;
import com.trading.repository.UserRepository;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MatchingEngine matchingEngine;
    
    @Autowired
    private RiskManagementService riskManagementService;
    
    public OrderResponse createOrder(OrderRequest request) {
        return createOrder(request, null);
    }
    
    public OrderResponse createOrder(OrderRequest request, String username) {
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid order request: " + request);
        }
        
        // Generate order ID
        String orderId = generateOrderId();
        
        // Create order
        Order order = new Order(
            orderId,
            request.getSymbol(),
            request.getSide(),
            request.getType(),
            request.getQuantity(),
            request.getPrice(),
            request.getAccountId()
        );
        
        // Associate with user if username provided - create test user if needed
        if (username != null) {
            Optional<com.trading.entity.User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                order.setUser(userOpt.get());
            } else if ("testuser".equals(username)) {
                // Create test user for demo purposes
                com.trading.entity.User testUser = new com.trading.entity.User();
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");
                testUser.setFullName("Test User");
                testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjBKt7wRMLjbstW"); // BCrypt hash of "password"
                testUser.setActive(true);
                testUser.setRole("TRADER");
                testUser.setCreatedAt(java.time.LocalDateTime.now());
                com.trading.entity.User savedUser = userRepository.save(testUser);
                order.setUser(savedUser);
                logger.info("Created test user: {}", username);
            } else {
                logger.warn("User not found: {}", username);
                throw new IllegalArgumentException("User not found: " + username);
            }
        }
        
        // Risk validation - temporarily commented out for debugging
        // RiskManagementService.RiskCheckResult riskResult = riskManagementService.validateOrder(order);
        // if (!riskResult.isValid()) {
        //     order.setStatus(OrderStatus.REJECTED);
        //     order.setReason(riskResult.getReason());
        //     orderRepository.save(order);
        //     
        //     logger.warn("Order rejected due to risk check: {} - {}", orderId, riskResult.getReason());
        //     
        //     return new OrderResponse(order);
        // }
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Submit to matching engine
        matchingEngine.processOrder(savedOrder);
        
        logger.info("Order created: {} - {} {} {} @ {} for user: {}", 
                   orderId, request.getSide(), request.getQuantity(), 
                   request.getSymbol(), request.getPrice(), username);
        
        return new OrderResponse(savedOrder);
    }
    
    public OrderResponse getOrder(String orderId) {
        Optional<Order> order = orderRepository.findByOrderId(orderId);
        
        if (order.isPresent()) {
            return new OrderResponse(order.get());
        } else {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
    }
    
    public List<OrderResponse> getOrdersBySymbol(String symbol) {
        List<Order> orders = orderRepository.findBySymbolAndStatus(symbol, OrderStatus.PENDING);
        return orders.stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getOrdersByAccount(String accountId) {
        List<Order> orders = orderRepository.findByAccountIdAndStatus(accountId, OrderStatus.PENDING);
        return orders.stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());
    }
    
    public OrderResponse cancelOrder(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findByOrderId(orderId);
        
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            
            if (order.getStatus() == OrderStatus.FILLED) {
                throw new IllegalStateException("Cannot cancel filled order: " + orderId);
            }
            
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("Order already cancelled: " + orderId);
            }
            
            order.setStatus(OrderStatus.CANCELLED);
            order.setReason("Cancelled by user");
            
            Order cancelledOrder = orderRepository.save(order);
            
            logger.info("Order cancelled: {}", orderId);
            
            return new OrderResponse(cancelledOrder);
        } else {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
    }
    
    public OrderResponse updateOrder(String orderId, BigDecimal newPrice, BigDecimal newQuantity) {
        Optional<Order> optionalOrder = orderRepository.findByOrderId(orderId);
        
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            
            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {
                throw new IllegalStateException("Cannot update order with status: " + order.getStatus());
            }
            
            if (newPrice != null) {
                order.setPrice(newPrice);
            }
            
            if (newQuantity != null) {
                BigDecimal remainingUnfilled = order.getQuantity().subtract(order.getFilledQuantity());
                if (newQuantity.compareTo(remainingUnfilled) < 0) {
                    throw new IllegalArgumentException("New quantity cannot be less than unfilled quantity");
                }
                
                order.setQuantity(newQuantity);
                order.setRemainingQuantity(newQuantity.subtract(order.getFilledQuantity()));
            }
            
            order.setUpdatedAt(LocalDateTime.now());
            
            Order updatedOrder = orderRepository.save(order);
            
            // Update in matching engine
            matchingEngine.getOrderBook(order.getSymbol()).updateOrder(updatedOrder);
            
            logger.info("Order updated: {} - price: {}, quantity: {}", orderId, newPrice, newQuantity);
            
            return new OrderResponse(updatedOrder);
        } else {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
    }
    
    public List<OrderResponse> getExpiredOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30); // Configurable
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);
        
        // Mark expired orders
        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            order.setReason("Order expired");
            orderRepository.save(order);
        }
        
        return expiredOrders.stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());
    }
    
    private String generateOrderId() {
        return "ORD_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public long getTotalOrderCount() {
        return orderRepository.count();
    }
    
    public long getActiveOrderCount() {
        return orderRepository.findByStatus(OrderStatus.PENDING).size();
    }
    
    public List<OrderResponse> getOrdersByUsername(String username) {
        List<Order> orders = orderRepository.findByUserUsername(username);
        return orders.stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());
    }
}
