package com.trading.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

@RestController
@RequestMapping("/api/xml")
@CrossOrigin(origins = "*")
public class XMLProcessingController {

    @PostMapping("/parse")
    public ResponseEntity<?> parseXML(@RequestBody Map<String, String> request) {
        try {
            String xmlContent = request.get("xmlContent");
            if (xmlContent == null || xmlContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "XML content is required"));
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            Map<String, Object> parsedData = new HashMap<>();
            Element root = document.getDocumentElement();
            
            // Parse the XML structure
            parsedData = parseElement(root);
            
            // Determine message type
            String messageType = determineMessageType(document);
            
            // Count fields
            int fieldCount = countFields(document);

            Map<String, Object> response = new HashMap<>();
            response.put("parsedData", parsedData);
            response.put("messageType", messageType);
            response.put("fieldCount", fieldCount);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to parse XML: " + e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateXML(@RequestBody Map<String, String> request) {
        try {
            String xmlContent = request.get("xmlContent");
            String messageType = request.get("messageType");
            
            if (xmlContent == null || xmlContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "XML content is required"));
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            // Basic validation
            boolean isValid = true;
            List<String> errors = new ArrayList<>();
            String validationDetails = "XML is well-formed";

            // Validate based on message type
            if (messageType != null) {
                switch (messageType) {
                    case "NewOrderSingle":
                        isValid = validateNewOrderSingle(document, errors);
                        break;
                    case "OrderCancelRequest":
                        isValid = validateOrderCancelRequest(document, errors);
                        break;
                    default:
                        validationDetails = "Generic XML validation performed";
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("messageType", messageType);
            response.put("validationDetails", validationDetails);
            response.put("errors", errors);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to validate XML: " + e.getMessage()));
        }
    }

    @PostMapping("/transform")
    public ResponseEntity<?> transformXMLToJSON(@RequestBody Map<String, String> request) {
        try {
            String xmlContent = request.get("xmlContent");
            if (xmlContent == null || xmlContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "XML content is required"));
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            Map<String, Object> jsonData = parseElement(document.getDocumentElement());

            Map<String, Object> response = new HashMap<>();
            response.put("jsonData", jsonData);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to transform XML: " + e.getMessage()));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<?> processBatchXML(@RequestBody Map<String, String> request) {
        try {
            String xmlBatch = request.get("xmlBatch");
            if (xmlBatch == null || xmlBatch.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "XML batch content is required"));
            }

            // Split by double newlines to separate XML messages
            String[] xmlMessages = xmlBatch.split("\\n\\s*\\n");
            
            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;

            for (int i = 0; i < xmlMessages.length; i++) {
                String xmlMessage = xmlMessages[i].trim();
                if (xmlMessage.isEmpty()) continue;

                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new ByteArrayInputStream(xmlMessage.getBytes()));

                    Map<String, Object> parsedData = parseElement(document.getDocumentElement());
                    String messageType = determineMessageType(document);

                    Map<String, Object> result = new HashMap<>();
                    result.put("index", i + 1);
                    result.put("status", "SUCCESS");
                    result.put("messageType", messageType);
                    result.put("data", parsedData);
                    results.add(result);
                    successCount++;
                } catch (Exception e) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("index", i + 1);
                    result.put("status", "ERROR");
                    result.put("error", e.getMessage());
                    results.add(result);
                    errorCount++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("processedCount", xmlMessages.length);
            response.put("successCount", successCount);
            response.put("errorCount", errorCount);
            response.put("results", results);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to process batch: " + e.getMessage()));
        }
    }

    private Map<String, Object> parseElement(Element element) {
        Map<String, Object> result = new HashMap<>();
        
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String name = childElement.getNodeName();
                
                if (childElement.getChildNodes().getLength() == 1 && 
                    childElement.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    // Leaf node with text content
                    result.put(name, childElement.getTextContent());
                } else {
                    // Node with child elements
                    result.put(name, parseElement(childElement));
                }
            }
        }
        
        return result;
    }

    private String determineMessageType(Document document) {
        Element root = document.getDocumentElement();
        
        // Check for common trading message types
        if (root.getElementsByTagName("NewOrderSingle").getLength() > 0) {
            return "NewOrderSingle";
        } else if (root.getElementsByTagName("OrderCancelRequest").getLength() > 0) {
            return "OrderCancelRequest";
        } else if (root.getElementsByTagName("ExecutionReport").getLength() > 0) {
            return "ExecutionReport";
        } else if (root.getElementsByTagName("MarketDataRequest").getLength() > 0) {
            return "MarketDataRequest";
        }
        
        return "Unknown";
    }

    private int countFields(Document document) {
        return countElementsRecursively(document.getDocumentElement());
    }

    private int countElementsRecursively(Element element) {
        int count = 1; // Count this element
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                count += countElementsRecursively((Element) child);
            }
        }
        return count;
    }

    private boolean validateNewOrderSingle(Document document, List<String> errors) {
        boolean isValid = true;
        
        // Check for required fields
        String[] requiredFields = {"ClOrdID", "Symbol", "Side", "OrderQty", "OrdType"};
        
        for (String field : requiredFields) {
            if (document.getElementsByTagName(field).getLength() == 0) {
                errors.add("Missing required field: " + field);
                isValid = false;
            }
        }
        
        return isValid;
    }

    private boolean validateOrderCancelRequest(Document document, List<String> errors) {
        boolean isValid = true;
        
        // Check for required fields
        String[] requiredFields = {"OrigClOrdID", "ClOrdID", "Symbol", "Side"};
        
        for (String field : requiredFields) {
            if (document.getElementsByTagName(field).getLength() == 0) {
                errors.add("Missing required field: " + field);
                isValid = false;
            }
        }
        
        return isValid;
    }
}
