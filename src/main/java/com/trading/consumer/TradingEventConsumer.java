package com.trading.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TradingEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingEventConsumer.class);
    
    @KafkaListener(topics = "order-updates", groupId = "trading-platform-group")
    public void handleOrderUpdate(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Received order update - Key: {}, Message: {}", key, message);
        // Process order update event
        // This could trigger notifications, audit logging, etc.
    }
    
    @KafkaListener(topics = "trades", groupId = "trading-platform-group")
    public void handleTrade(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Received trade - Key: {}, Message: {}", key, message);
        // Process trade event
        // This could trigger P&L calculations, risk updates, etc.
    }
    
    @KafkaListener(topics = "order-rejections", groupId = "trading-platform-group")
    public void handleOrderRejection(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Received order rejection - Key: {}, Message: {}", key, message);
        // Process order rejection event
        // This could trigger notifications, compliance reporting, etc.
    }
    
    @KafkaListener(topics = "ioi-creations", groupId = "trading-platform-group")
    public void handleIOICreation(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Received IOI creation - Key: {}, Message: {}", key, message);
        // Process IOI creation event
        // This could trigger notifications to interested parties
    }
    
    @KafkaListener(topics = "ioi-updates", groupId = "trading-platform-group")
    public void handleIOIUpdate(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Received IOI update - Key: {}, Message: {}", key, message);
        // Process IOI update event
        // This could trigger notifications, status updates, etc.
    }
    
    @KafkaListener(topics = "ioi-expirations", groupId = "trading-platform-group")
    public void handleIOIExpiration(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Received IOI expiration - Key: {}, Message: {}", key, message);
        // Process IOI expiration event
        // This could trigger cleanup, notifications, etc.
    }
}
