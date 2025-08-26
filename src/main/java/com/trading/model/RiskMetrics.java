package com.trading.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_metrics")
public class RiskMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id", nullable = false)
    private String accountId;
    
    @Column(name = "value_at_risk", precision = 15, scale = 2)
    private BigDecimal valueAtRisk;
    
    @Column(name = "max_drawdown", precision = 5, scale = 2)
    private BigDecimal maxDrawdown;
    
    @Column(name = "sharpe_ratio", precision = 5, scale = 2)
    private BigDecimal sharpeRatio;
    
    @Column(name = "max_concentration", precision = 5, scale = 2)
    private BigDecimal maxConcentration;
    
    @Column(name = "risk_score")
    private Integer riskScore;
    
    @Column(name = "calculation_time", nullable = false)
    private LocalDateTime calculationTime;
    
    @Column(name = "portfolio_value", precision = 15, scale = 2)
    private BigDecimal portfolioValue;
    
    @Column(name = "daily_pnl", precision = 15, scale = 2)
    private BigDecimal dailyPnl;
    
    @Column(name = "weekly_pnl", precision = 15, scale = 2)
    private BigDecimal weeklyPnl;
    
    // Constructors
    public RiskMetrics() {
        this.calculationTime = LocalDateTime.now();
    }
    
    public RiskMetrics(String accountId) {
        this();
        this.accountId = accountId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    
    public BigDecimal getValueAtRisk() { return valueAtRisk; }
    public void setValueAtRisk(BigDecimal valueAtRisk) { this.valueAtRisk = valueAtRisk; }
    
    public BigDecimal getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
    
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    
    public BigDecimal getMaxConcentration() { return maxConcentration; }
    public void setMaxConcentration(BigDecimal maxConcentration) { this.maxConcentration = maxConcentration; }
    
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    
    public LocalDateTime getCalculationTime() { return calculationTime; }
    public void setCalculationTime(LocalDateTime calculationTime) { this.calculationTime = calculationTime; }
    
    public BigDecimal getPortfolioValue() { return portfolioValue; }
    public void setPortfolioValue(BigDecimal portfolioValue) { this.portfolioValue = portfolioValue; }
    
    public BigDecimal getDailyPnl() { return dailyPnl; }
    public void setDailyPnl(BigDecimal dailyPnl) { this.dailyPnl = dailyPnl; }
    
    public BigDecimal getWeeklyPnl() { return weeklyPnl; }
    public void setWeeklyPnl(BigDecimal weeklyPnl) { this.weeklyPnl = weeklyPnl; }
}
