package com.trading.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.trading.domain.IOIStatus;
import com.trading.domain.IndicationOfInterest;
import com.trading.domain.OrderSide;
import com.trading.repository.IOIRepository;
import com.trading.xml.IOIMessage;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

@Service
public class IOIService {
    
    private static final Logger logger = LoggerFactory.getLogger(IOIService.class);
    
    @Autowired
    private IOIRepository ioiRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private final JAXBContext jaxbContext;
    
    public IOIService() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(IOIMessage.class);
    }
    
    public IndicationOfInterest createIOI(String symbol, OrderSide side, BigDecimal quantity,
                                         BigDecimal price, String brokerId, String clientId) {
        String ioiId = generateIOIId();
        
        IndicationOfInterest ioi = new IndicationOfInterest(ioiId, symbol, side, quantity, 
                                                           price, brokerId, clientId);
        
        // Convert to XML and store
        try {
            String xmlMessage = convertToXML(ioi);
            ioi.setXmlMessage(xmlMessage);
        } catch (JAXBException e) {
            logger.error("Error converting IOI to XML: {}", ioiId, e);
        }
        
        IndicationOfInterest savedIOI = ioiRepository.save(ioi);
        
        // Publish to Kafka
        publishIOICreation(savedIOI);
        
        logger.info("IOI created: {} - {} {} @ {}", ioiId, quantity, symbol, price);
        
        return savedIOI;
    }
    
    public IndicationOfInterest processXMLIOI(String xmlMessage) throws JAXBException {
        IOIMessage ioiMsg = parseXMLMessage(xmlMessage);
        
        // Convert to domain object
        OrderSide side = OrderSide.valueOf(ioiMsg.getSide().toUpperCase());
        
        IndicationOfInterest ioi = new IndicationOfInterest(
            ioiMsg.getIoiId(),
            ioiMsg.getSymbol(),
            side,
            ioiMsg.getQuantity(),
            ioiMsg.getPrice(),
            ioiMsg.getBrokerId(),
            ioiMsg.getClientId()
        );
        
        if (ioiMsg.getExpiryTime() != null) {
            ioi.setExpiresAt(ioiMsg.getExpiryTime());
        }
        
        if (ioiMsg.getNotes() != null) {
            ioi.setNotes(ioiMsg.getNotes());
        }
        
        ioi.setXmlMessage(xmlMessage);
        
        IndicationOfInterest savedIOI = ioiRepository.save(ioi);
        
        // Publish to Kafka
        publishIOICreation(savedIOI);
        
        logger.info("IOI processed from XML: {} - {} {} @ {}", 
                   ioi.getIoiId(), ioi.getQuantity(), ioi.getSymbol(), ioi.getPrice());
        
        return savedIOI;
    }
    
    public Optional<IndicationOfInterest> getIOI(String ioiId) {
        IndicationOfInterest ioi = ioiRepository.findByIoiId(ioiId);
        return Optional.ofNullable(ioi);
    }
    
    public List<IndicationOfInterest> getIOIsBySymbol(String symbol) {
        return ioiRepository.findBySymbol(symbol);
    }
    
    public List<IndicationOfInterest> getActiveIOIs() {
        return ioiRepository.findActiveIOIs(IOIStatus.ACTIVE, LocalDateTime.now());
    }
    
    public List<IndicationOfInterest> getIOIsByBroker(String brokerId) {
        return ioiRepository.findByBrokerId(brokerId);
    }
    
    public List<IndicationOfInterest> getIOIsByClient(String clientId) {
        return ioiRepository.findByClientId(clientId);
    }
    
    public IndicationOfInterest updateIOIStatus(String ioiId, IOIStatus newStatus) {
        IndicationOfInterest ioi = ioiRepository.findByIoiId(ioiId);
        
        if (ioi != null) {
            ioi.setStatus(newStatus);
            
            IndicationOfInterest updatedIOI = ioiRepository.save(ioi);
            
            // Publish status update to Kafka
            publishIOIStatusUpdate(updatedIOI);
            
            logger.info("IOI status updated: {} -> {}", ioiId, newStatus);
            
            return updatedIOI;
        } else {
            throw new IllegalArgumentException("IOI not found: " + ioiId);
        }
    }
    
    public void cancelIOI(String ioiId) {
        updateIOIStatus(ioiId, IOIStatus.CANCELLED);
    }
    
    public void expireIOI(String ioiId) {
        updateIOIStatus(ioiId, IOIStatus.EXPIRED);
    }
    
    public void cleanupExpiredIOIs() {
        List<IndicationOfInterest> expiredIOIs = ioiRepository.findByExpiresAtBefore(LocalDateTime.now());
        
        for (IndicationOfInterest ioi : expiredIOIs) {
            if (ioi.getStatus() == IOIStatus.ACTIVE) {
                ioi.setStatus(IOIStatus.EXPIRED);
                ioiRepository.save(ioi);
                
                // Publish expiration to Kafka
                publishIOIExpiration(ioi);
                
                logger.info("IOI expired: {}", ioi.getIoiId());
            }
        }
    }
    
    private String generateIOIId() {
        return "IOI_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String convertToXML(IndicationOfInterest ioi) throws JAXBException {
        IOIMessage ioiMsg = new IOIMessage(
            ioi.getIoiId(),
            ioi.getSymbol(),
            ioi.getSide().toString(),
            ioi.getQuantity(),
            ioi.getPrice(),
            ioi.getBrokerId(),
            ioi.getClientId()
        );
        
        ioiMsg.setTimestamp(ioi.getCreatedAt());
        ioiMsg.setExpiryTime(ioi.getExpiresAt());
        ioiMsg.setNotes(ioi.getNotes());
        
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(ioiMsg, writer);
        
        return writer.toString();
    }
    
    private IOIMessage parseXMLMessage(String xmlMessage) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xmlMessage);
        
        return (IOIMessage) unmarshaller.unmarshal(reader);
    }
    
    private void publishIOICreation(IndicationOfInterest ioi) {
        kafkaTemplate.send("ioi-creations", ioi.getIoiId(), ioi.toString());
    }
    
    private void publishIOIStatusUpdate(IndicationOfInterest ioi) {
        kafkaTemplate.send("ioi-updates", ioi.getIoiId(), ioi.toString());
    }
    
    private void publishIOIExpiration(IndicationOfInterest ioi) {
        kafkaTemplate.send("ioi-expirations", ioi.getIoiId(), ioi.toString());
    }
}
