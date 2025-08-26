package com.trading.service;

import com.trading.domain.Order;
import com.trading.domain.OrderSide;
import com.trading.model.RiskMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Risk Management Service for High-Touch Trading
 * Implements comprehensive risk controls and monitoring
 */
@Service
public class EnhancedRiskManagementService {

    @Autowired
    private OrderService orderService;

    // Risk limits configuration
    private final Map<String, BigDecimal> positionLimits = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> orderValueLimits = new ConcurrentHashMap<>();
    private final Set<String> restrictedSymbols = new HashSet<>();
    private final Map<String, Integer> dailyOrderCounts = new ConcurrentHashMap<>();
    
    // Risk metrics tracking
    private final Map<String, RiskMetrics> accountRiskMetrics = new ConcurrentHashMap<>();
    
    public EnhancedRiskManagementService() {
        initializeRiskLimits();
    }

    private void initializeRiskLimits() {
        // Default position limits (shares)
        positionLimits.put("DEFAULT", new BigDecimal("10000"));
        positionLimits.put("AAPL", new BigDecimal("5000"));
        positionLimits.put("TSLA", new BigDecimal("1000"));
        
        // Default order value limits (USD)
        orderValueLimits.put("DEFAULT", new BigDecimal("1000000"));
        orderValueLimits.put("JUNIOR_TRADER", new BigDecimal("100000"));
        orderValueLimits.put("SENIOR_TRADER", new BigDecimal("5000000"));
        
        // Restricted symbols for trading
        restrictedSymbols.addAll(Arrays.asList("RESTRICTED1", "RESTRICTED2"));
    }

    /**
     * Comprehensive order validation with multiple risk checks
     */
    public boolean validateOrder(Order order) {
        try {
            // Basic validation
            if (!validateBasicOrderParameters(order)) {
                return false;
            }
            
            // Symbol restriction check
            if (isSymbolRestricted(order.getSymbol())) {
                order.setReason("Symbol is restricted for trading");
                return false;
            }
            
            // Position limit check
            if (!validatePositionLimits(order)) {
                order.setReason("Position limits exceeded");
                return false;
            }
            
            // Order value limit check
            if (!validateOrderValueLimits(order)) {
                order.setReason("Order value limits exceeded");
                return false;
            }
            
            // Daily trading limit check
            if (!validateDailyTradingLimits(order)) {
                order.setReason("Daily trading limits exceeded");
                return false;
            }
            
            // Concentration risk check
            if (!validateConcentrationRisk(order)) {
                order.setReason("Concentration risk limits exceeded");
                return false;
            }
            
            // Market hours check
            if (!validateMarketHours(order)) {
                order.setReason("Trading outside market hours");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            order.setReason("Risk validation error: " + e.getMessage());
            return false;
        }
    }

    private boolean validateBasicOrderParameters(Order order) {
        return order.getSymbol() != null && !order.getSymbol().isEmpty() &&
               order.getQuantity().compareTo(BigDecimal.ZERO) > 0 &&
               order.getPrice().compareTo(BigDecimal.ZERO) > 0 &&
               order.getAccountId() != null && !order.getAccountId().isEmpty();
    }

    private boolean isSymbolRestricted(String symbol) {
        return restrictedSymbols.contains(symbol.toUpperCase());
    }

    private boolean validatePositionLimits(Order order) {
        String symbol = order.getSymbol();
        BigDecimal limit = positionLimits.getOrDefault(symbol, positionLimits.get("DEFAULT"));
        
        // Get current position for the symbol
        BigDecimal currentPosition = BigDecimal.valueOf(getCurrentPosition(order.getAccountId(), symbol));
        BigDecimal newPosition = order.getSide() == OrderSide.BUY ? 
            currentPosition.add(order.getQuantity()) : 
            currentPosition.subtract(order.getQuantity());
            
        return newPosition.abs().compareTo(limit) <= 0;
    }

    private boolean validateOrderValueLimits(Order order) {
        BigDecimal orderValue = order.getQuantity().multiply(order.getPrice());
        String accountType = getAccountType(order.getAccountId());
        BigDecimal limit = orderValueLimits.getOrDefault(accountType, orderValueLimits.get("DEFAULT"));
        
        return orderValue.compareTo(limit) <= 0;
    }

    private boolean validateDailyTradingLimits(Order order) {
        String accountId = order.getAccountId();
        int dailyCount = dailyOrderCounts.getOrDefault(accountId, 0);
        int maxDailyOrders = getMaxDailyOrders(accountId);
        
        return dailyCount < maxDailyOrders;
    }

    private boolean validateConcentrationRisk(Order order) {
        String accountId = order.getAccountId();
        String symbol = order.getSymbol();
        
        // Calculate portfolio concentration after this order
        Map<String, Integer> portfolio = getAccountPortfolio(accountId);
        BigDecimal totalPortfolioValue = calculatePortfolioValue(portfolio);
        
        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return true; // First trade
        }
        
        BigDecimal symbolValue = BigDecimal.valueOf(portfolio.getOrDefault(symbol, 0))
            .multiply(BigDecimal.valueOf(150.0)); // Simplified price
        BigDecimal concentration = symbolValue.divide(totalPortfolioValue, 4, BigDecimal.ROUND_HALF_UP);
        
        return concentration.compareTo(new BigDecimal("0.25")) <= 0; // Max 25% concentration
    }

    private boolean validateMarketHours(Order order) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // Simple market hours check (9:30 AM - 4:00 PM EST)
        return hour >= 9 && hour < 16;
    }

    /**
     * Calculate real-time risk metrics for an account
     */
    public RiskMetrics calculateRiskMetrics(String accountId) {
        RiskMetrics metrics = new RiskMetrics();
        metrics.setAccountId(accountId);
        metrics.setCalculationTime(LocalDateTime.now());
        
        Map<String, Integer> portfolio = getAccountPortfolio(accountId);
        BigDecimal portfolioValue = calculatePortfolioValue(portfolio);
        
        // Calculate VaR (Value at Risk) - simplified 1-day 95% VaR
        BigDecimal var95 = portfolioValue.multiply(new BigDecimal("0.02")); // 2% of portfolio
        metrics.setValueAtRisk(var95);
        
        // Calculate maximum drawdown
        BigDecimal maxDrawdown = calculateMaxDrawdown(accountId);
        metrics.setMaxDrawdown(maxDrawdown);
        
        // Calculate Sharpe ratio (simplified)
        BigDecimal sharpeRatio = calculateSharpeRatio(accountId);
        metrics.setSharpeRatio(sharpeRatio);
        
        // Calculate position concentration
        BigDecimal maxConcentration = calculateMaxConcentration(portfolio, portfolioValue);
        metrics.setMaxConcentration(maxConcentration);
        
        // Overall risk score (1-10 scale)
        int riskScore = calculateOverallRiskScore(metrics);
        metrics.setRiskScore(riskScore);
        
        accountRiskMetrics.put(accountId, metrics);
        return metrics;
    }

    /**
     * Generate risk report for compliance and monitoring
     */
    public String generateRiskReport(String accountId) {
        RiskMetrics metrics = calculateRiskMetrics(accountId);
        StringBuilder report = new StringBuilder();
        
        report.append("=== RISK MANAGEMENT REPORT ===\n");
        report.append("Account: ").append(accountId).append("\n");
        report.append("Timestamp: ").append(LocalDateTime.now()).append("\n\n");
        
        report.append("Risk Metrics:\n");
        report.append("- Value at Risk (95%): $").append(metrics.getValueAtRisk()).append("\n");
        report.append("- Maximum Drawdown: ").append(metrics.getMaxDrawdown()).append("%\n");
        report.append("- Sharpe Ratio: ").append(metrics.getSharpeRatio()).append("\n");
        report.append("- Max Concentration: ").append(metrics.getMaxConcentration()).append("%\n");
        report.append("- Overall Risk Score: ").append(metrics.getRiskScore()).append("/10\n\n");
        
        // Position limits status
        report.append("Position Limits Status:\n");
        Map<String, Integer> portfolio = getAccountPortfolio(accountId);
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            String symbol = entry.getKey();
            Integer position = entry.getValue();
            BigDecimal limit = positionLimits.getOrDefault(symbol, positionLimits.get("DEFAULT"));
            double utilization = (Math.abs(position) / limit.doubleValue()) * 100;
            report.append("- ").append(symbol).append(": ").append(position)
                  .append(" shares (").append(String.format("%.1f", utilization)).append("% of limit)\n");
        }
        
        return report.toString();
    }

    /**
     * Real-time risk monitoring with alerts
     */
    public List<String> checkRiskAlerts(String accountId) {
        List<String> alerts = new ArrayList<>();
        RiskMetrics metrics = calculateRiskMetrics(accountId);
        
        // High risk score alert
        if (metrics.getRiskScore() >= 8) {
            alerts.add("HIGH RISK: Overall risk score is " + metrics.getRiskScore() + "/10");
        }
        
        // High concentration alert
        if (metrics.getMaxConcentration().compareTo(new BigDecimal("20")) > 0) {
            alerts.add("CONCENTRATION RISK: Maximum position concentration is " + 
                      metrics.getMaxConcentration() + "%");
        }
        
        // High VaR alert
        BigDecimal portfolioValue = calculatePortfolioValue(getAccountPortfolio(accountId));
        if (portfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal varPercentage = metrics.getValueAtRisk()
                .divide(portfolioValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
            if (varPercentage.compareTo(new BigDecimal("5")) > 0) {
                alerts.add("HIGH VAR: Value at Risk is " + varPercentage + "% of portfolio");
            }
        }
        
        // Daily trading limit alert
        int dailyCount = dailyOrderCounts.getOrDefault(accountId, 0);
        int maxDaily = getMaxDailyOrders(accountId);
        if (dailyCount > maxDaily * 0.8) {
            alerts.add("TRADING LIMIT: " + dailyCount + "/" + maxDaily + " daily orders used");
        }
        
        return alerts;
    }

    // Helper methods
    private int getCurrentPosition(String accountId, String symbol) {
        // In real implementation, query from database
        return 0; // Simplified for demo
    }

    private String getAccountType(String accountId) {
        // In real implementation, lookup account type from database
        return accountId.startsWith("JUNIOR") ? "JUNIOR_TRADER" : "SENIOR_TRADER";
    }

    private int getMaxDailyOrders(String accountId) {
        String accountType = getAccountType(accountId);
        return accountType.equals("JUNIOR_TRADER") ? 50 : 200;
    }

    private Map<String, Integer> getAccountPortfolio(String accountId) {
        // In real implementation, query from database
        Map<String, Integer> portfolio = new HashMap<>();
        portfolio.put("AAPL", 100);
        portfolio.put("MSFT", 50);
        return portfolio;
    }

    private BigDecimal calculatePortfolioValue(Map<String, Integer> portfolio) {
        // In real implementation, use current market prices
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            BigDecimal price = getMarketPrice(entry.getKey());
            BigDecimal value = price.multiply(BigDecimal.valueOf(Math.abs(entry.getValue())));
            totalValue = totalValue.add(value);
        }
        return totalValue;
    }

    private BigDecimal getMarketPrice(String symbol) {
        // Simplified market prices
        Map<String, BigDecimal> prices = Map.of(
            "AAPL", new BigDecimal("150.50"),
            "MSFT", new BigDecimal("300.00"),
            "GOOGL", new BigDecimal("2500.00")
        );
        return prices.getOrDefault(symbol, new BigDecimal("100.00"));
    }

    private BigDecimal calculateMaxDrawdown(String accountId) {
        // Simplified calculation
        return new BigDecimal("5.2"); // 5.2%
    }

    private BigDecimal calculateSharpeRatio(String accountId) {
        // Simplified calculation
        return new BigDecimal("1.8");
    }

    private BigDecimal calculateMaxConcentration(Map<String, Integer> portfolio, BigDecimal totalValue) {
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal maxValue = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            BigDecimal price = getMarketPrice(entry.getKey());
            BigDecimal value = price.multiply(BigDecimal.valueOf(Math.abs(entry.getValue())));
            if (value.compareTo(maxValue) > 0) {
                maxValue = value;
            }
        }
        
        return maxValue.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }

    private int calculateOverallRiskScore(RiskMetrics metrics) {
        int score = 1;
        
        // Increase score based on various risk factors
        if (metrics.getMaxConcentration().compareTo(new BigDecimal("20")) > 0) score += 2;
        if (metrics.getMaxConcentration().compareTo(new BigDecimal("30")) > 0) score += 2;
        if (metrics.getMaxDrawdown().compareTo(new BigDecimal("10")) > 0) score += 2;
        if (metrics.getSharpeRatio().compareTo(new BigDecimal("1.0")) < 0) score += 2;
        
        return Math.min(score, 10);
    }

    // Configuration methods
    public void updatePositionLimit(String symbol, BigDecimal limit) {
        positionLimits.put(symbol, limit);
    }

    public void addRestrictedSymbol(String symbol) {
        restrictedSymbols.add(symbol.toUpperCase());
    }

    public void removeRestrictedSymbol(String symbol) {
        restrictedSymbols.remove(symbol.toUpperCase());
    }
}
