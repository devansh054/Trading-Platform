package com.trading.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderBook {
    
    private final String symbol;
    private final ConcurrentSkipListMap<BigDecimal, List<Order>> bids; // Price -> Orders (descending)
    private final ConcurrentSkipListMap<BigDecimal, List<Order>> asks; // Price -> Orders (ascending)
    private final Map<String, Order> orderMap;
    
    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.bids = new ConcurrentSkipListMap<>(Collections.reverseOrder());
        this.asks = new ConcurrentSkipListMap<>();
        this.orderMap = new ConcurrentHashMap<>();
    }
    
    public void addOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
        
        if (order.getSide() == OrderSide.BUY) {
            addToPriceLevel(bids, order);
        } else {
            addToPriceLevel(asks, order);
        }
    }
    
    public void removeOrder(String orderId) {
        Order order = orderMap.remove(orderId);
        if (order != null) {
            if (order.getSide() == OrderSide.BUY) {
                removeFromPriceLevel(bids, order);
            } else {
                removeFromPriceLevel(asks, order);
            }
        }
    }
    
    public void updateOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
        
        if (order.getSide() == OrderSide.BUY) {
            updatePriceLevel(bids, order);
        } else {
            updatePriceLevel(asks, order);
        }
    }
    
    private void addToPriceLevel(ConcurrentSkipListMap<BigDecimal, List<Order>> priceLevels, Order order) {
        priceLevels.computeIfAbsent(order.getPrice(), k -> new ArrayList<>()).add(order);
    }
    
    private void removeFromPriceLevel(ConcurrentSkipListMap<BigDecimal, List<Order>> priceLevels, Order order) {
        priceLevels.computeIfPresent(order.getPrice(), (price, orders) -> {
            orders.removeIf(o -> o.getOrderId().equals(order.getOrderId()));
            return orders.isEmpty() ? null : orders;
        });
    }
    
    private void updatePriceLevel(ConcurrentSkipListMap<BigDecimal, List<Order>> priceLevels, Order order) {
        removeFromPriceLevel(priceLevels, order);
        addToPriceLevel(priceLevels, order);
    }
    
    public BigDecimal getBestBid() {
        return bids.isEmpty() ? null : bids.firstKey();
    }
    
    public BigDecimal getBestAsk() {
        return asks.isEmpty() ? null : asks.firstKey();
    }
    
    public BigDecimal getSpread() {
        BigDecimal bestBid = getBestBid();
        BigDecimal bestAsk = getBestAsk();
        
        if (bestBid != null && bestAsk != null) {
            return bestAsk.subtract(bestBid);
        }
        return null;
    }
    
    public List<Order> getBidsAtPrice(BigDecimal price) {
        return bids.getOrDefault(price, new ArrayList<>());
    }
    
    public List<Order> getAsksAtPrice(BigDecimal price) {
        return asks.getOrDefault(price, new ArrayList<>());
    }
    
    public Map<BigDecimal, BigDecimal> getBidLevels(int maxLevels) {
        Map<BigDecimal, BigDecimal> levels = new LinkedHashMap<>();
        int count = 0;
        
        for (Map.Entry<BigDecimal, List<Order>> entry : bids.entrySet()) {
            if (count >= maxLevels) break;
            
            BigDecimal totalQuantity = entry.getValue().stream()
                .map(Order::getRemainingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            levels.put(entry.getKey(), totalQuantity);
            count++;
        }
        
        return levels;
    }
    
    public Map<BigDecimal, BigDecimal> getAskLevels(int maxLevels) {
        Map<BigDecimal, BigDecimal> levels = new LinkedHashMap<>();
        int count = 0;
        
        for (Map.Entry<BigDecimal, List<Order>> entry : asks.entrySet()) {
            if (count >= maxLevels) break;
            
            BigDecimal totalQuantity = entry.getValue().stream()
                .map(Order::getRemainingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            levels.put(entry.getKey(), totalQuantity);
            count++;
        }
        
        return levels;
    }
    
    public Order getOrder(String orderId) {
        return orderMap.get(orderId);
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public int getTotalBidOrders() {
        return orderMap.values().stream()
            .filter(o -> o.getSide() == OrderSide.BUY && o.getStatus() == OrderStatus.PENDING)
            .mapToInt(o -> 1)
            .sum();
    }
    
    public int getTotalAskOrders() {
        return orderMap.values().stream()
            .filter(o -> o.getSide() == OrderSide.SELL && o.getStatus() == OrderStatus.PENDING)
            .mapToInt(o -> 1)
            .sum();
    }
}
