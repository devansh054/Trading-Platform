package com.trading.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_messages")
public class TradeMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "message_type", nullable = false)
    private String messageType;
    
    @Column(name = "order_id")
    private String orderId;
    
    @Column(name = "trade_id")
    private String tradeId;
    
    @Column(name = "symbol")
    private String symbol;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "reject_reason")
    private String rejectReason;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "raw_message", columnDefinition = "TEXT")
    private String rawMessage;
    
    @Column(name = "processed_by")
    private String processedBy;
    
    // Constructors
    public TradeMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public TradeMessage(String messageType, String status) {
        this();
        this.messageType = messageType;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }
    
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
}
