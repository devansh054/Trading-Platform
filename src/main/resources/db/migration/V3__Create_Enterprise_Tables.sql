-- Database migration script for enterprise trading features
-- Creates tables for trade messages, risk metrics, and system monitoring

-- Trade Messages table
CREATE TABLE IF NOT EXISTS trade_messages (
    id BIGSERIAL PRIMARY KEY,
    message_type VARCHAR(50) NOT NULL,
    order_id VARCHAR(100),
    trade_id VARCHAR(100),
    symbol VARCHAR(20),
    status VARCHAR(20) NOT NULL,
    reject_reason TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    raw_message TEXT,
    processed_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Risk Metrics table
CREATE TABLE IF NOT EXISTS risk_metrics (
    id BIGSERIAL PRIMARY KEY,
    account_id VARCHAR(100) NOT NULL,
    value_at_risk DECIMAL(15,2),
    max_drawdown DECIMAL(5,2),
    sharpe_ratio DECIMAL(5,2),
    max_concentration DECIMAL(5,2),
    risk_score INTEGER,
    calculation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    portfolio_value DECIMAL(15,2),
    daily_pnl DECIMAL(15,2),
    weekly_pnl DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Monitoring Logs table
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGSERIAL PRIMARY KEY,
    log_level VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    component VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_code VARCHAR(50),
    stack_trace TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Metrics table
CREATE TABLE IF NOT EXISTS performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4),
    metric_type VARCHAR(50), -- COUNTER, GAUGE, TIMER
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tags JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Position Limits table
CREATE TABLE IF NOT EXISTS position_limits (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    limit_value DECIMAL(15,2) NOT NULL,
    limit_type VARCHAR(50) DEFAULT 'POSITION', -- POSITION, ORDER_VALUE, DAILY_COUNT
    account_id VARCHAR(100),
    effective_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expiry_date DATE,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(symbol, limit_type, account_id, effective_date)
);

-- Restricted Symbols table
CREATE TABLE IF NOT EXISTS restricted_symbols (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    restriction_type VARCHAR(50) DEFAULT 'TRADING', -- TRADING, REPORTING, COMPLIANCE
    reason TEXT,
    effective_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expiry_date DATE,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Risk Alerts table
CREATE TABLE IF NOT EXISTS risk_alerts (
    id BIGSERIAL PRIMARY KEY,
    account_id VARCHAR(100) NOT NULL,
    alert_type VARCHAR(50) NOT NULL, -- HIGH_RISK, CONCENTRATION, VAR_BREACH, LIMIT_BREACH
    alert_level VARCHAR(20) NOT NULL, -- INFO, WARNING, CRITICAL
    message TEXT NOT NULL,
    threshold_value DECIMAL(15,4),
    actual_value DECIMAL(15,4),
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(100),
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_trade_messages_timestamp ON trade_messages(timestamp);
CREATE INDEX IF NOT EXISTS idx_trade_messages_status ON trade_messages(status);
CREATE INDEX IF NOT EXISTS idx_trade_messages_symbol ON trade_messages(symbol);
CREATE INDEX IF NOT EXISTS idx_trade_messages_order_id ON trade_messages(order_id);

CREATE INDEX IF NOT EXISTS idx_risk_metrics_account_id ON risk_metrics(account_id);
CREATE INDEX IF NOT EXISTS idx_risk_metrics_calculation_time ON risk_metrics(calculation_time);

CREATE INDEX IF NOT EXISTS idx_system_logs_timestamp ON system_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_system_logs_level ON system_logs(log_level);
CREATE INDEX IF NOT EXISTS idx_system_logs_component ON system_logs(component);

CREATE INDEX IF NOT EXISTS idx_performance_metrics_name ON performance_metrics(metric_name);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_timestamp ON performance_metrics(timestamp);

CREATE INDEX IF NOT EXISTS idx_position_limits_symbol ON position_limits(symbol);
CREATE INDEX IF NOT EXISTS idx_position_limits_account ON position_limits(account_id);
CREATE INDEX IF NOT EXISTS idx_position_limits_effective ON position_limits(effective_date);

CREATE INDEX IF NOT EXISTS idx_restricted_symbols_symbol ON restricted_symbols(symbol);
CREATE INDEX IF NOT EXISTS idx_restricted_symbols_effective ON restricted_symbols(effective_date);

CREATE INDEX IF NOT EXISTS idx_risk_alerts_account ON risk_alerts(account_id);
CREATE INDEX IF NOT EXISTS idx_risk_alerts_type ON risk_alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_risk_alerts_level ON risk_alerts(alert_level);
CREATE INDEX IF NOT EXISTS idx_risk_alerts_acknowledged ON risk_alerts(acknowledged);

-- Insert some sample data for testing
INSERT INTO position_limits (symbol, limit_value, limit_type, created_by) VALUES
('AAPL', 5000, 'POSITION', 'SYSTEM'),
('MSFT', 3000, 'POSITION', 'SYSTEM'),
('GOOGL', 1000, 'POSITION', 'SYSTEM'),
('TSLA', 1000, 'POSITION', 'SYSTEM'),
('DEFAULT', 10000, 'POSITION', 'SYSTEM');

INSERT INTO restricted_symbols (symbol, restriction_type, reason, created_by) VALUES
('RESTRICTED1', 'TRADING', 'Compliance restriction', 'SYSTEM'),
('RESTRICTED2', 'TRADING', 'Risk management restriction', 'SYSTEM');

-- Sample performance metrics
INSERT INTO performance_metrics (metric_name, metric_value, metric_type) VALUES
('order_processing_time_ms', 150.5, 'TIMER'),
('ioi_processing_time_ms', 75.2, 'TIMER'),
('total_orders_count', 1250, 'COUNTER'),
('total_ioi_count', 850, 'COUNTER'),
('database_connection_count', 8, 'GAUGE'),
('memory_usage_percent', 65.5, 'GAUGE');

-- Sample system logs
INSERT INTO system_logs (log_level, message, component) VALUES
('INFO', 'System started successfully', 'APPLICATION'),
('INFO', 'Database connection established', 'DATABASE'),
('INFO', 'Kafka consumer started', 'MESSAGING'),
('WARNING', 'High memory usage detected', 'MONITORING'),
('INFO', 'Order processing completed', 'ORDER_SERVICE');
