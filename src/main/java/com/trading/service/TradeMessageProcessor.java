package com.trading.service;

import com.trading.model.TradeMessage;
import com.trading.domain.Order;
import com.trading.domain.OrderSide;
import com.trading.domain.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced XML Trade Message Processing Service
 * Handles FIX-like XML messages for high-touch trading operations
 */
@Service
public class TradeMessageProcessor {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RiskManagementService riskManagementService;

    /**
     * Process incoming trade messages in XML format
     * Supports multiple message types: NewOrderSingle, OrderCancelRequest, etc.
     */
    public TradeMessage processTradeMessage(String xmlMessage) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new org.xml.sax.InputSource(new StringReader(xmlMessage)));
            
            Element root = document.getDocumentElement();
            String messageType = root.getTagName();
            
            switch (messageType) {
                case "NewOrderSingle":
                    return processNewOrderSingle(root);
                case "OrderCancelRequest":
                    return processOrderCancelRequest(root);
                case "MarketDataRequest":
                    return processMarketDataRequest(root);
                case "TradeReport":
                    return processTradeReport(root);
                default:
                    throw new IllegalArgumentException("Unsupported message type: " + messageType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing trade message", e);
        }
    }

    private TradeMessage processNewOrderSingle(Element orderElement) {
        TradeMessage message = new TradeMessage();
        message.setMessageType("NewOrderSingle");
        message.setTimestamp(LocalDateTime.now());
        
        // Extract order details from XML
        String symbol = getElementValue(orderElement, "Symbol");
        String side = getElementValue(orderElement, "Side");
        String orderType = getElementValue(orderElement, "OrdType");
        String quantityStr = getElementValue(orderElement, "OrderQty");
        String priceStr = getElementValue(orderElement, "Price");
        String account = getElementValue(orderElement, "Account");
        
        // Create order object with proper validation
        Order order = new Order();
        order.setSymbol(symbol);
        
        // Handle side enum conversion
        try {
            order.setSide(OrderSide.valueOf(side.toUpperCase()));
        } catch (IllegalArgumentException e) {
            message.setStatus("REJECTED");
            message.setRejectReason("Invalid order side: " + side);
            return message;
        }
        
        // Handle order type enum conversion (map LIMIT to appropriate enum value)
        try {
            if ("LIMIT".equalsIgnoreCase(orderType)) {
                order.setType(OrderType.LIMIT);
            } else if ("MARKET".equalsIgnoreCase(orderType)) {
                order.setType(OrderType.MARKET);
            } else {
                order.setType(OrderType.valueOf(orderType.toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            message.setStatus("REJECTED");
            message.setRejectReason("Invalid order type: " + orderType);
            return message;
        }
        
        // Handle numeric conversions
        try {
            order.setQuantity(new BigDecimal(quantityStr));
            if (priceStr != null && !priceStr.isEmpty()) {
                order.setPrice(new BigDecimal(priceStr));
            }
        } catch (NumberFormatException e) {
            message.setStatus("REJECTED");
            message.setRejectReason("Invalid quantity or price format");
            return message;
        }
        
        order.setAccountId(account);
        
        // Risk validation
        RiskManagementService.RiskCheckResult riskResult = riskManagementService.validateOrder(order);
        if (!riskResult.isValid()) {
            message.setStatus("REJECTED");
            message.setRejectReason(riskResult.getReason());
            return message;
        }
        
        // For demo purposes, mock the order processing
        message.setStatus("ACCEPTED");
        message.setOrderId("ORDER_" + System.currentTimeMillis());
        
        return message;
    }

    private TradeMessage processOrderCancelRequest(Element cancelElement) {
        TradeMessage message = new TradeMessage();
        message.setMessageType("OrderCancelRequest");
        message.setTimestamp(LocalDateTime.now());
        
        String orderId = getElementValue(cancelElement, "OrigClOrdID");
        String symbol = getElementValue(cancelElement, "Symbol");
        
        try {
            orderService.cancelOrder(orderId);
            message.setStatus("ACCEPTED");
            message.setOrderId(orderId);
        } catch (Exception e) {
            message.setStatus("REJECTED");
            message.setRejectReason("Order not found or cannot be cancelled");
        }
        
        return message;
    }

    private TradeMessage processMarketDataRequest(Element mdElement) {
        TradeMessage message = new TradeMessage();
        message.setMessageType("MarketDataRequest");
        message.setTimestamp(LocalDateTime.now());
        
        String symbol = getElementValue(mdElement, "Symbol");
        String mdReqType = getElementValue(mdElement, "MDReqType");
        
        // Process market data request
        message.setStatus("ACCEPTED");
        message.setSymbol(symbol);
        
        return message;
    }

    private TradeMessage processTradeReport(Element tradeElement) {
        TradeMessage message = new TradeMessage();
        message.setMessageType("TradeReport");
        message.setTimestamp(LocalDateTime.now());
        
        String tradeId = getElementValue(tradeElement, "TradeID");
        String symbol = getElementValue(tradeElement, "Symbol");
        BigDecimal quantity = new BigDecimal(getElementValue(tradeElement, "LastQty"));
        BigDecimal price = new BigDecimal(getElementValue(tradeElement, "LastPx"));
        
        message.setStatus("PROCESSED");
        message.setTradeId(tradeId);
        message.setSymbol(symbol);
        
        return message;
    }

    /**
     * Generate XML trade confirmation message
     */
    public String generateTradeConfirmation(Order order) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            
            Element root = document.createElement("TradeConfirmation");
            document.appendChild(root);
            
            addElement(document, root, "OrderID", order.getOrderId());
            addElement(document, root, "Symbol", order.getSymbol());
            addElement(document, root, "Side", order.getSide().toString());
            addElement(document, root, "Quantity", String.valueOf(order.getQuantity()));
            addElement(document, root, "Price", String.valueOf(order.getPrice()));
            addElement(document, root, "Status", order.getStatus().toString());
            addElement(document, root, "Timestamp", LocalDateTime.now().toString());
            
            return documentToString(document);
        } catch (Exception e) {
            throw new RuntimeException("Error generating trade confirmation", e);
        }
    }

    /**
     * Parse batch trade messages for high-volume processing
     */
    public List<TradeMessage> processBatchTradeMessages(String batchXml) {
        List<TradeMessage> messages = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new org.xml.sax.InputSource(new StringReader(batchXml)));
            
            NodeList messageNodes = document.getElementsByTagName("TradeMessage");
            
            for (int i = 0; i < messageNodes.getLength(); i++) {
                Element messageElement = (Element) messageNodes.item(i);
                String messageXml = elementToString(messageElement);
                TradeMessage message = processTradeMessage(messageXml);
                messages.add(message);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing batch trade messages", e);
        }
        
        return messages;
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    private void addElement(Document document, Element parent, String tagName, String value) {
        Element element = document.createElement(tagName);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    private String documentToString(Document document) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private String elementToString(Element element) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }
}
