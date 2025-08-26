package com.trading.controller;

import com.trading.domain.IndicationOfInterest;
import com.trading.domain.IOIStatus;
import com.trading.domain.OrderSide;
import com.trading.service.IOIService;
import com.trading.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ioi")
@CrossOrigin(origins = "*")
public class IOIController {
    
    @Autowired
    private IOIService ioiService;
    
    @Autowired
    private AuthService authService;
    
    @PostMapping
    public ResponseEntity<IndicationOfInterest> createIOI(@RequestBody Map<String, Object> request, @RequestHeader(value = "Authorization", required = false) String token) {
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
            
            String symbol = (String) request.get("symbol");
            OrderSide side = OrderSide.valueOf(((String) request.get("side")).toUpperCase());
            BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
            BigDecimal price = request.get("price") != null ? 
                new BigDecimal(request.get("price").toString()) : null;
            String brokerId = (String) request.get("brokerId");
            
            // Use authenticated user's username as clientId
            IndicationOfInterest ioi = ioiService.createIOI(symbol, side, quantity, price, brokerId, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(ioi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/xml")
    public ResponseEntity<IndicationOfInterest> processXMLIOI(@RequestBody String xmlMessage) {
        try {
            IndicationOfInterest ioi = ioiService.processXMLIOI(xmlMessage);
            return ResponseEntity.status(HttpStatus.CREATED).body(ioi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{ioiId}")
    public ResponseEntity<IndicationOfInterest> getIOI(@PathVariable String ioiId) {
        try {
            var ioi = ioiService.getIOI(ioiId);
            if (ioi.isPresent()) {
                return ResponseEntity.ok(ioi.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<IndicationOfInterest>> getIOIsBySymbol(@PathVariable String symbol) {
        try {
            List<IndicationOfInterest> iois = ioiService.getIOIsBySymbol(symbol);
            return ResponseEntity.ok(iois);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<IndicationOfInterest>> getActiveIOIs(@RequestHeader(value = "Authorization", required = false) String token) {
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
            
            // Get user-specific IOIs by using clientId as username
            List<IndicationOfInterest> iois = ioiService.getIOIsByClient(username);
            return ResponseEntity.ok(iois);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/broker/{brokerId}")
    public ResponseEntity<List<IndicationOfInterest>> getIOIsByBroker(@PathVariable String brokerId) {
        try {
            List<IndicationOfInterest> iois = ioiService.getIOIsByBroker(brokerId);
            return ResponseEntity.ok(iois);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<IndicationOfInterest>> getIOIsByClient(@PathVariable String clientId) {
        try {
            List<IndicationOfInterest> iois = ioiService.getIOIsByClient(clientId);
            return ResponseEntity.ok(iois);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{ioiId}/status")
    public ResponseEntity<IndicationOfInterest> updateIOIStatus(
            @PathVariable String ioiId,
            @RequestBody Map<String, String> request) {
        try {
            IOIStatus newStatus = IOIStatus.valueOf(request.get("status").toUpperCase());
            IndicationOfInterest ioi = ioiService.updateIOIStatus(ioiId, newStatus);
            return ResponseEntity.ok(ioi);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{ioiId}/cancel")
    public ResponseEntity<IndicationOfInterest> cancelIOI(@PathVariable String ioiId) {
        try {
            ioiService.cancelIOI(ioiId);
            var ioi = ioiService.getIOI(ioiId);
            if (ioi.isPresent()) {
                return ResponseEntity.ok(ioi.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/cleanup/expired")
    public ResponseEntity<String> cleanupExpiredIOIs() {
        try {
            ioiService.cleanupExpiredIOIs();
            return ResponseEntity.ok("Expired IOIs cleaned up successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
