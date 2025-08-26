package com.trading.controller;

import com.trading.domain.Order;
import com.trading.model.TradeMessage;
import com.trading.model.RiskMetrics;
import com.trading.service.TradeMessageProcessor;
import com.trading.service.EnhancedRiskManagementService;
import com.trading.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * High-Touch Trading Desk Controller
 * Specialized endpoints for institutional trading operations
 */
@RestController
@RequestMapping("/api/high-touch")
@CrossOrigin(origins = "*")
public class HighTouchTradingController {

    @Autowired
    private TradeMessageProcessor tradeMessageProcessor;

    @Autowired
    private EnhancedRiskManagementService riskManagementService;

    @Autowired
    private OrderService orderService;

    /**
     * Process XML trade messages (FIX-like format)
     */
    @PostMapping(value = "/trade-message", consumes = "text/plain")
    public ResponseEntity<?> processTradeMessage(@RequestBody String xmlMessage) {
        try {
            System.out.println("Received XML message: " + xmlMessage);
            TradeMessage result = tradeMessageProcessor.processTradeMessage(xmlMessage);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error processing trade message: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process trade message");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Process batch trade messages for high-volume operations
     */
    @PostMapping("/batch-trade-messages")
    public ResponseEntity<?> processBatchTradeMessages(@RequestBody String batchXml) {
        try {
            List<TradeMessage> results = tradeMessageProcessor.processBatchTradeMessages(batchXml);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", results.size());
            response.put("messages", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process batch trade messages");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Generate trade confirmation XML
     */
    @GetMapping("/trade-confirmation/{orderId}")
    public ResponseEntity<?> generateTradeConfirmation(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("status", "SUCCESS");
        response.put("confirmationXml", "<TradeConfirmation><OrderID>" + orderId + "</OrderID><Symbol>AAPL</Symbol><Status>FILLED</Status></TradeConfirmation>");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Get comprehensive risk metrics for an account
     */
    @GetMapping("/risk-metrics/{accountId}")
    public ResponseEntity<?> getRiskMetrics(@PathVariable String accountId) {
        try {
            RiskMetrics metrics = riskManagementService.calculateRiskMetrics(accountId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to calculate risk metrics");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Generate detailed risk report
     */
    @GetMapping("/risk-report/{accountId}")
    public ResponseEntity<?> getRiskReport(@PathVariable String accountId) {
        try {
            String report = riskManagementService.generateRiskReport(accountId);
            Map<String, String> response = new HashMap<>();
            response.put("accountId", accountId);
            response.put("report", report);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate risk report");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get real-time risk alerts
     */
    @GetMapping("/risk-alerts/{accountId}")
    public ResponseEntity<?> getRiskAlerts(@PathVariable String accountId) {
        try {
            List<String> alerts = riskManagementService.checkRiskAlerts(accountId);
            Map<String, Object> response = new HashMap<>();
            response.put("accountId", accountId);
            response.put("alertCount", alerts.size());
            response.put("alerts", alerts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to check risk alerts");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Validate order against risk limits before submission
     */
    @PostMapping("/validate-order")
    public ResponseEntity<?> validateOrder(@RequestBody Order order) {
        try {
            boolean isValid = riskManagementService.validateOrder(order);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("orderId", order.getOrderId());
            if (!isValid && order.getReason() != null) {
                response.put("reason", order.getReason());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate order");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update position limits for symbols
     */
    @PostMapping("/update-position-limit")
    public ResponseEntity<?> updatePositionLimit(@RequestBody Map<String, Object> request) {
        try {
            String symbol = (String) request.get("symbol");
            Number limitNumber = (Number) request.get("limit");
            
            if (symbol == null || limitNumber == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Symbol and limit are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            java.math.BigDecimal limit = new java.math.BigDecimal(limitNumber.toString());
            riskManagementService.updatePositionLimit(symbol, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("symbol", symbol);
            response.put("newLimit", limit);
            response.put("status", "updated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update position limit");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Add symbol to restricted list
     */
    @PostMapping("/restrict-symbol")
    public ResponseEntity<?> restrictSymbol(@RequestBody Map<String, String> request) {
        try {
            String symbol = request.get("symbol");
            if (symbol == null || symbol.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Symbol is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            riskManagementService.addRestrictedSymbol(symbol);
            
            Map<String, String> response = new HashMap<>();
            response.put("symbol", symbol);
            response.put("status", "restricted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to restrict symbol");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Remove symbol from restricted list
     */
    @PostMapping("/unrestrict-symbol")
    public ResponseEntity<?> unrestrictSymbol(@RequestBody Map<String, String> request) {
        try {
            String symbol = request.get("symbol");
            if (symbol == null || symbol.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Symbol is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            riskManagementService.removeRestrictedSymbol(symbol);
            
            Map<String, String> response = new HashMap<>();
            response.put("symbol", symbol);
            response.put("status", "unrestricted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to unrestrict symbol");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get trading desk statistics and metrics
     */
    @GetMapping("/desk-stats")
    public ResponseEntity<?> getDeskStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get basic trading statistics
            // Mock data for demo
            stats.put("totalOrders", 1250);
            stats.put("activeOrders", 85);
            stats.put("dailyVolume", 2500000);
            stats.put("timestamp", java.time.LocalDateTime.now());
            
            // Add risk summary
            Map<String, Object> riskSummary = new HashMap<>();
            riskSummary.put("highRiskAccounts", 0); // Would be calculated from actual data
            riskSummary.put("totalAlerts", 0);
            riskSummary.put("restrictedSymbols", 2);
            stats.put("riskSummary", riskSummary);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get desk statistics");
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
