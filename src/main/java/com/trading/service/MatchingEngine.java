package com.trading.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.trading.domain.Order;
import com.trading.domain.OrderBook;
import com.trading.domain.OrderSide;
import com.trading.domain.OrderStatus;
import com.trading.domain.OrderType;
import com.trading.domain.Trade;
import com.trading.repository.OrderRepository;
import com.trading.repository.TradeRepository;

@Service
public class MatchingEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(MatchingEngine.class);
    
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TradeRepository tradeRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${trading.matching-engine.thread-pool-size:4}")
    private int threadPoolSize;
    
    @Value("${trading.matching-engine.order-timeout-seconds:30}")
    private int orderTimeoutSeconds;
    
    public MatchingEngine() {
        // Initialize with default values, will be updated by @PostConstruct
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    @PostConstruct
    public void initialize() {
        // Shutdown the default executor and create a new one with the configured size
        if (this.executorService != null) {
            this.executorService.shutdown();
        }
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    public void processOrder(Order order) {
        executorService.submit(() -> {
            try {
                matchOrder(order);
            } catch (Exception e) {
                logger.error("Error processing order: {}", order.getOrderId(), e);
                rejectOrder(order, "Processing error: " + e.getMessage());
            }
        });
    }
    
    private void matchOrder(Order order) {
        String symbol = order.getSymbol();
        OrderBook orderBook = getOrCreateOrderBook(symbol);
        
        // Add order to order book
        orderBook.addOrder(order);
        orderRepository.save(order);
        
        // Try to match the order
        List<Trade> trades = new ArrayList<>();
        
        if (order.getSide() == OrderSide.BUY) {
            trades = matchBuyOrder(order, orderBook);
        } else {
            trades = matchSellOrder(order, orderBook);
        }
        
        // Process trades
        for (Trade trade : trades) {
            processTrade(trade);
        }
        
        // Update order book
        orderBook.updateOrder(order);
        orderRepository.save(order);
        
        // Publish order update to Kafka
        publishOrderUpdate(order);
    }
    
    private List<Trade> matchBuyOrder(Order buyOrder, OrderBook orderBook) {
        List<Trade> trades = new ArrayList<>();
        BigDecimal remainingQuantity = buyOrder.getRemainingQuantity();
        
        // Get all ask orders sorted by price (ascending)
        Map<BigDecimal, BigDecimal> askLevels = orderBook.getAskLevels(100);
        
        for (Map.Entry<BigDecimal, BigDecimal> entry : askLevels.entrySet()) {
            BigDecimal askPrice = entry.getKey();
            
            // Check if buy order can match at this price level
            if (buyOrder.getType() == OrderType.LIMIT && buyOrder.getPrice().compareTo(askPrice) < 0) {
                break; // Price too high for limit order
            }
            
            List<Order> askOrders = orderBook.getAsksAtPrice(askPrice);
            
            for (Order askOrder : askOrders) {
                if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                
                if (askOrder.getStatus() != OrderStatus.PENDING) {
                    continue;
                }
                
                BigDecimal matchQuantity = remainingQuantity.min(askOrder.getRemainingQuantity());
                BigDecimal matchPrice = askPrice;
                
                // Create trade
                Trade trade = createTrade(buyOrder, askOrder, matchQuantity, matchPrice);
                trades.add(trade);
                
                // Update quantities
                remainingQuantity = remainingQuantity.subtract(matchQuantity);
                buyOrder.updateFilledQuantity(matchQuantity);
                askOrder.updateFilledQuantity(matchQuantity);
                
                // Update order book
                if (askOrder.isFullyFilled()) {
                    orderBook.removeOrder(askOrder.getOrderId());
                } else {
                    orderBook.updateOrder(askOrder);
                }
                
                orderBook.updateOrder(buyOrder);
            }
        }
        
        return trades;
    }
    
    private List<Trade> matchSellOrder(Order sellOrder, OrderBook orderBook) {
        List<Trade> trades = new ArrayList<>();
        BigDecimal remainingQuantity = sellOrder.getRemainingQuantity();
        
        // Get all bid orders sorted by price (descending)
        Map<BigDecimal, BigDecimal> bidLevels = orderBook.getBidLevels(100);
        
        for (Map.Entry<BigDecimal, BigDecimal> entry : bidLevels.entrySet()) {
            BigDecimal bidPrice = entry.getKey();
            
            // Check if sell order can match at this price level
            if (sellOrder.getType() == OrderType.LIMIT && sellOrder.getPrice().compareTo(bidPrice) > 0) {
                break; // Price too low for limit order
            }
            
            List<Order> bidOrders = orderBook.getBidsAtPrice(bidPrice);
            
            for (Order bidOrder : bidOrders) {
                if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                
                if (bidOrder.getStatus() != OrderStatus.PENDING) {
                    continue;
                }
                
                BigDecimal matchQuantity = remainingQuantity.min(bidOrder.getRemainingQuantity());
                BigDecimal matchPrice = bidPrice;
                
                // Create trade
                Trade trade = createTrade(bidOrder, sellOrder, matchQuantity, matchPrice);
                trades.add(trade);
                
                // Update quantities
                remainingQuantity = remainingQuantity.subtract(matchQuantity);
                sellOrder.updateFilledQuantity(matchQuantity);
                bidOrder.updateFilledQuantity(matchQuantity);
                
                // Update order book
                if (bidOrder.isFullyFilled()) {
                    orderBook.removeOrder(bidOrder.getOrderId());
                } else {
                    orderBook.updateOrder(bidOrder);
                }
                
                orderBook.updateOrder(sellOrder);
            }
        }
        
        return trades;
    }
    
    private Trade createTrade(Order buyOrder, Order sellOrder, BigDecimal quantity, BigDecimal price) {
        String tradeId = generateTradeId();
        
        Trade trade = new Trade(tradeId, buyOrder.getSymbol(), quantity, price,
                               buyOrder.getOrderId(), sellOrder.getOrderId(),
                               buyOrder.getAccountId(), sellOrder.getAccountId());
        
        tradeRepository.save(trade);
        
        // Publish trade to Kafka
        publishTrade(trade);
        
        return trade;
    }
    
    private void processTrade(Trade trade) {
        logger.info("Trade executed: {} - {} {} @ {}", 
                   trade.getTradeId(), trade.getQuantity(), trade.getSymbol(), trade.getPrice());
    }
    
    private void rejectOrder(Order order, String reason) {
        order.setStatus(OrderStatus.REJECTED);
        order.setReason(reason);
        orderRepository.save(order);
        
        // Publish order rejection to Kafka
        publishOrderRejection(order, reason);
    }
    
    private OrderBook getOrCreateOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, OrderBook::new);
    }
    
    private String generateTradeId() {
        return "TRADE_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void publishOrderUpdate(Order order) {
        // Publish to Kafka topic for order updates
        kafkaTemplate.send("order-updates", order.getOrderId(), order.toString());
    }
    
    private void publishTrade(Trade trade) {
        // Publish to Kafka topic for trade notifications
        kafkaTemplate.send("trades", trade.getTradeId(), trade.toString());
    }
    
    private void publishOrderRejection(Order order, String reason) {
        // Publish to Kafka topic for order rejections
        kafkaTemplate.send("order-rejections", order.getOrderId(), reason);
    }
    
    public OrderBook getOrderBook(String symbol) {
        return orderBooks.get(symbol);
    }
    
    public Map<String, OrderBook> getAllOrderBooks() {
        return new HashMap<>(orderBooks);
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
