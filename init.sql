-- Trading Platform Database Initialization Script
-- This script creates the initial database schema for the trading platform

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create sequences for auto-incrementing IDs
CREATE SEQUENCE IF NOT EXISTS order_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS trade_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS ioi_id_seq START 1;

-- Create the orders table (matching Java entity exactly)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY DEFAULT nextval('order_id_seq'),
    order_id VARCHAR(255) UNIQUE NOT NULL,
    symbol VARCHAR(255) NOT NULL,
    side VARCHAR(255) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    type VARCHAR(255) NOT NULL CHECK (type IN ('MARKET', 'LIMIT')),
    quantity DECIMAL(19,4) NOT NULL CHECK (quantity > 0),
    price DECIMAL(19,4) CHECK (price > 0),
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED', 'REJECTED', 'EXPIRED')),
    filled_quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    remaining_quantity DECIMAL(19,4) NOT NULL DEFAULT 0,
    account_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    filled_at TIMESTAMP,
    reason VARCHAR(255)
);

-- Create the trades table (matching Java entity exactly)
CREATE TABLE IF NOT EXISTS trades (
    id BIGINT PRIMARY KEY DEFAULT nextval('trade_id_seq'),
    trade_id VARCHAR(255) UNIQUE NOT NULL,
    symbol VARCHAR(255) NOT NULL,
    quantity DECIMAL(19,4) NOT NULL CHECK (quantity > 0),
    price DECIMAL(19,4) NOT NULL CHECK (price > 0),
    total_value DECIMAL(19,4) NOT NULL,
    buy_order_id VARCHAR(255) NOT NULL,
    sell_order_id VARCHAR(255) NOT NULL,
    buy_account_id VARCHAR(255) NOT NULL,
    sell_account_id VARCHAR(255) NOT NULL,
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_venue VARCHAR(255)
);

-- Create the indications_of_interest table (matching Java entity exactly)
CREATE TABLE IF NOT EXISTS indications_of_interest (
    id BIGINT PRIMARY KEY DEFAULT nextval('ioi_id_seq'),
    ioi_id VARCHAR(255) UNIQUE NOT NULL,
    symbol VARCHAR(255) NOT NULL,
    side VARCHAR(255) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity DECIMAL(19,4) NOT NULL CHECK (quantity > 0),
    price DECIMAL(19,4) CHECK (price > 0),
    broker_id VARCHAR(255) NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED', 'FILLED', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    expires_at TIMESTAMP,
    notes VARCHAR(255),
    xml_message TEXT
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_orders_symbol ON orders(symbol);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_account_id ON orders(account_id);
CREATE INDEX IF NOT EXISTS idx_orders_side ON orders(side);
CREATE INDEX IF NOT EXISTS idx_orders_type ON orders(type);

CREATE INDEX IF NOT EXISTS idx_trades_symbol ON trades(symbol);
CREATE INDEX IF NOT EXISTS idx_trades_executed_at ON trades(executed_at);

CREATE INDEX IF NOT EXISTS idx_ioi_symbol ON indications_of_interest(symbol);
CREATE INDEX IF NOT EXISTS idx_ioi_status ON indications_of_interest(status);
CREATE INDEX IF NOT EXISTS idx_ioi_created_at ON indications_of_interest(created_at);
CREATE INDEX IF NOT EXISTS idx_ioi_broker_id ON indications_of_interest(broker_id);
CREATE INDEX IF NOT EXISTS idx_ioi_client_id ON indications_of_interest(client_id);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_orders_updated_at 
    BEFORE UPDATE ON orders 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ioi_updated_at 
    BEFORE UPDATE ON indications_of_interest 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample data (optional)
INSERT INTO orders (order_id, symbol, side, type, quantity, price, account_id, remaining_quantity) VALUES
('ORD001', 'AAPL', 'BUY', 'LIMIT', 100.0, 150.50, 'user1', 100.0),
('ORD002', 'GOOGL', 'SELL', 'MARKET', 50.0, NULL, 'user2', 50.0)
ON CONFLICT (order_id) DO NOTHING;

-- Grant permissions (adjust as needed for your setup)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO trading_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO trading_user;
