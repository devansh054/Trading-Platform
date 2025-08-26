package com.trading.xml;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "IOI")
@XmlAccessorType(XmlAccessType.FIELD)
public class IOIMessage {
    
    @XmlElement(name = "IOIID", required = true)
    private String ioiId;
    
    @XmlElement(name = "Symbol", required = true)
    private String symbol;
    
    @XmlElement(name = "Side", required = true)
    private String side;
    
    @XmlElement(name = "Quantity", required = true)
    private BigDecimal quantity;
    
    @XmlElement(name = "Price")
    private BigDecimal price;
    
    @XmlElement(name = "BrokerID", required = true)
    private String brokerId;
    
    @XmlElement(name = "ClientID", required = true)
    private String clientId;
    
    @XmlElement(name = "Timestamp", required = true)
    private LocalDateTime timestamp;
    
    @XmlElement(name = "ExpiryTime")
    private LocalDateTime expiryTime;
    
    @XmlElement(name = "Notes")
    private String notes;
    
    // Constructors
    public IOIMessage() {}
    
    public IOIMessage(String ioiId, String symbol, String side, BigDecimal quantity,
                      BigDecimal price, String brokerId, String clientId) {
        this.ioiId = ioiId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.brokerId = brokerId;
        this.clientId = clientId;
        this.timestamp = LocalDateTime.now();
        this.expiryTime = LocalDateTime.now().plusHours(24);
    }
    
    // Getters and Setters
    public String getIoiId() { return ioiId; }
    public void setIoiId(String ioiId) { this.ioiId = ioiId; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getBrokerId() { return brokerId; }
    public void setBrokerId(String brokerId) { this.brokerId = brokerId; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
