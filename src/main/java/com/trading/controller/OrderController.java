package com.trading.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.trading.domain.OrderStatus;
import com.trading.dto.OrderRequest;
import com.trading.dto.OrderResponse;
import com.trading.service.MatchingEngine;
import com.trading.service.OrderService;
import com.trading.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private MatchingEngine matchingEngine;
    
    @Autowired
    private AuthService authService;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            logger.info("Received order request: {}", request);
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(401).build();
            }
            
            // Validate token and get user
            Optional<com.trading.entity.User> userOpt = authService.validateToken(token.replace("Bearer ", ""));
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401).build();
            }
            String username = userOpt.get().getUsername();
            logger.info("Creating order for user: {}", username);
            
            OrderResponse response = orderService.createOrder(request, username);
            logger.info("Order created successfully: {}", response.getOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        try {
            OrderResponse response = orderService.getOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<OrderResponse>> getOrdersBySymbol(@PathVariable String symbol, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(401).build();
            }
        
            // Validate token and get user
            Optional<com.trading.entity.User> userOpt = authService.validateToken(token.replace("Bearer ", ""));
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401).build();
            }
            String username = userOpt.get().getUsername();
            List<OrderResponse> orders = orderService.getOrdersByUsername(username);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Optional<com.trading.entity.User> userOpt = authService.validateToken(token);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String username = userOpt.get().getUsername();
            List<OrderResponse> orders = orderService.getOrdersByUsername(username);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByAccount(@PathVariable String accountId, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Optional<com.trading.entity.User> userOpt = authService.validateToken(token);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<OrderResponse> orders = orderService.getOrdersByAccount(accountId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId) {
        try {
            OrderResponse response = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> updates) {
        try {
            BigDecimal newPrice = null;
            BigDecimal newQuantity = null;
            
            if (updates.containsKey("price")) {
                newPrice = new BigDecimal(updates.get("price").toString());
            }
            
            if (updates.containsKey("quantity")) {
                newQuantity = new BigDecimal(updates.get("quantity").toString());
            }
            
            OrderResponse response = orderService.updateOrder(orderId, newPrice, newQuantity);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/expired")
    public ResponseEntity<List<OrderResponse>> getExpiredOrders() {
        try {
            List<OrderResponse> orders = orderService.getExpiredOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Object>> getOrderCount() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderService.getTotalOrderCount());
        stats.put("activeOrders", orderService.getActiveOrderCount());
        return ResponseEntity.ok(stats);
    }
    
    
    @GetMapping("/orderbook/{symbol}")
    public ResponseEntity<Map<String, Object>> getOrderBook(@PathVariable String symbol) {
        try {
            var orderBook = matchingEngine.getOrderBook(symbol);
            
            if (orderBook == null) {
                // Return empty order book structure instead of 404
                Map<String, Object> emptyOrderBook = new HashMap<>();
                emptyOrderBook.put("symbol", symbol);
                emptyOrderBook.put("bestBid", null);
                emptyOrderBook.put("bestAsk", null);
                emptyOrderBook.put("spread", null);
                emptyOrderBook.put("bidLevels", new HashMap<>());
                emptyOrderBook.put("askLevels", new HashMap<>());
                emptyOrderBook.put("totalBidOrders", 0);
                emptyOrderBook.put("totalAskOrders", 0);
                return ResponseEntity.ok(emptyOrderBook);
            }
            
            Map<String, Object> orderBookData = new HashMap<>();
            orderBookData.put("symbol", orderBook.getSymbol());
            orderBookData.put("bestBid", orderBook.getBestBid());
            orderBookData.put("bestAsk", orderBook.getBestAsk());
            orderBookData.put("spread", orderBook.getSpread());
            orderBookData.put("bidLevels", orderBook.getBidLevels(10));
            orderBookData.put("askLevels", orderBook.getAskLevels(10));
            orderBookData.put("totalBidOrders", orderBook.getTotalBidOrders());
            orderBookData.put("totalAskOrders", orderBook.getTotalAskOrders());
            
            return ResponseEntity.ok(orderBookData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
