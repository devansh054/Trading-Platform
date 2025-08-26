package com.trading.controller;

import com.trading.service.SystemMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * System Monitoring and Troubleshooting Controller
 * Provides endpoints for system health monitoring and diagnostics
 */
@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class SystemMonitoringController {

    @Autowired
    private SystemMonitoringService monitoringService;

    /**
     * Get comprehensive system health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = monitoringService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * Get database health and performance metrics
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        Map<String, Object> dbHealth = monitoringService.checkDatabaseHealth();
        return ResponseEntity.ok(dbHealth);
    }

    /**
     * Get memory usage metrics
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryMetrics() {
        Map<String, Object> memory = monitoringService.getMemoryMetrics();
        return ResponseEntity.ok(memory);
    }

    /**
     * Get performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> performance = monitoringService.getPerformanceMetrics();
        return ResponseEntity.ok(performance);
    }

    /**
     * Get error metrics and recent errors
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorMetrics() {
        Map<String, Object> errors = monitoringService.getErrorMetrics();
        return ResponseEntity.ok(errors);
    }

    /**
     * Run comprehensive system diagnostics
     */
    @GetMapping("/diagnostics")
    public ResponseEntity<Map<String, Object>> runDiagnostics() {
        Map<String, Object> diagnostics = monitoringService.runDiagnostics();
        return ResponseEntity.ok(diagnostics);
    }

    /**
     * Get system logs with optional filtering
     */
    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getSystemLogs(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "50") int limit) {
        List<Map<String, Object>> logs = monitoringService.getSystemLogs(level, limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * Log a system event (for testing purposes)
     */
    @PostMapping("/log")
    public ResponseEntity<String> logEvent(@RequestBody Map<String, String> request) {
        String level = request.getOrDefault("level", "INFO");
        String message = request.get("message");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message is required");
        }
        
        monitoringService.logEvent(level, message);
        return ResponseEntity.ok("Event logged successfully");
    }
}
