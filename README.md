![image alt](https://github.com/devansh054/Trading-Platform/blob/4444f58491329b2fa35c58d6ef61ef04dfb85f53/trading_platform_page-0001.jpg)
# Mini Equities Trading Platform with IOI Support

A comprehensive trading platform built with Java 17, Spring Boot, PostgreSQL, and Apache Kafka, featuring real-time order matching, risk management, and Indications of Interest (IOI) processing.

## Features

### 🚀 Core Trading Engine
- **Order Management System**: Support for LIMIT and MARKET orders
- **Real-time Matching Engine**: Price-time priority order matching with partial fills
- **Order Book Management**: Real-time bid/ask price levels with depth visualization
- **Trade Execution**: Automatic trade execution with trade reporting

### 📊 Indications of Interest (IOI)
- **XML Message Processing**: JAXB-based XML parsing and generation
- **IOI Lifecycle Management**: Creation, updates, expiration, and cancellation
- **Broker and Client Management**: Multi-party IOI handling
- **Real-time IOI Broadcasting**: Kafka-based event streaming

### 🛡️ Risk Management
- **Position Limits**: Configurable maximum position sizes per symbol
- **Order Value Limits**: Maximum order value restrictions
- **Restricted Symbols**: Blacklist for prohibited trading symbols
- **Real-time Risk Monitoring**: Continuous risk validation

### 🔄 Event Streaming
- **Apache Kafka Integration**: Real-time event publishing and consumption
- **Event Topics**: Order updates, trades, rejections, IOI events
- **Asynchronous Processing**: Non-blocking event handling
- **Scalable Architecture**: Multi-consumer support

## Tech Stack

- **Java 17**: Latest LTS version with modern language features
- **Spring Boot 3.2.0**: Latest Spring framework with Jakarta EE support
- **PostgreSQL**: Robust relational database for trading data
- **Apache Kafka 3.6.0**: High-performance event streaming platform
- **JAXB**: XML binding for IOI message processing
- **Spring Data JPA**: Data access layer with repository pattern
- **Spring Web**: RESTful API endpoints
- **Spring Kafka**: Kafka integration and configuration

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │  Order Service  │    │ Matching Engine │
│   Controllers   │◄──►│                 │◄──►│                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   IOI Service   │    │ Risk Management │    │   Order Book   │
│                 │    │     Service     │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Kafka Topics  │    │   PostgreSQL    │    │   Event        │
│                 │    │                 │    │   Consumers    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 3.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd mini-equities-trading-platform
   ```

2. **Set up PostgreSQL**
   ```sql
   CREATE DATABASE trading_db;
   CREATE USER trading_user WITH PASSWORD 'trading_pass';
   GRANT ALL PRIVILEGES ON DATABASE trading_db TO trading_user;
   ```

3. **Set up Apache Kafka**
   ```bash
   # Start Zookeeper
   bin/zookeeper-server-start.sh config/zookeeper.properties
   
   # Start Kafka
   bin/kafka-server-start.sh config/server.properties
   ```

4. **Configure application properties**
   ```yaml
   # Update src/main/resources/application.yml with your database and Kafka settings
   ```

5. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Configuration

The application can be configured via `application.yml`:

```yaml
trading:
  matching-engine:
    thread-pool-size: 4
    order-timeout-seconds: 30
  
  risk-management:
    max-position-size: 10000
    max-order-value: 1000000
    restricted-symbols:
      - "RESTRICTED1"
      - "RESTRICTED2"
  
  order-book:
    max-price-levels: 100
    tick-size: 0.01
```

## API Endpoints

### Orders

- `POST /api/orders` - Create new order
- `GET /api/orders/{orderId}` - Get order details
- `GET /api/orders/symbol/{symbol}` - Get orders by symbol
- `GET /api/orders/account/{accountId}` - Get orders by account
- `PUT /api/orders/{orderId}/cancel` - Cancel order
- `PUT /api/orders/{orderId}` - Update order
- `GET /api/orders/orderbook/{symbol}` - Get order book for symbol

### IOI

- `POST /api/ioi` - Create new IOI
- `POST /api/ioi/xml` - Process XML IOI message
- `GET /api/ioi/{ioiId}` - Get IOI details
- `GET /api/ioi/symbol/{symbol}` - Get IOIs by symbol
- `GET /api/ioi/active` - Get active IOIs
- `PUT /api/ioi/{ioiId}/status` - Update IOI status
- `PUT /api/ioi/{ioiId}/cancel` - Cancel IOI

## Usage Examples

### Creating a Limit Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "side": "BUY",
    "type": "LIMIT",
    "quantity": 100,
    "price": 150.50,
    "accountId": "ACC001"
  }'
```

### Creating an IOI

```bash
curl -X POST http://localhost:8080/api/ioi \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "MSFT",
    "side": "SELL",
    "quantity": 500,
    "price": 300.00,
    "brokerId": "BROKER001",
    "clientId": "CLIENT001"
  }'
```

### Processing XML IOI

```bash
curl -X POST http://localhost:8080/api/ioi/xml \
  -H "Content-Type: text/plain" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<IOI>
  <IOIID>IOI_001</IOIID>
  <Symbol>GOOGL</Symbol>
  <Side>BUY</Side>
  <Quantity>200</Quantity>
  <Price>2500.00</Price>
  <BrokerID>BROKER002</BrokerID>
  <ClientID>CLIENT002</ClientID>
</IOI>'
```

## Kafka Topics

The platform uses the following Kafka topics for event streaming:

- `order-updates` - Order status and quantity updates
- `trades` - Executed trades
- `order-rejections` - Rejected orders with reasons
- `ioi-creations` - New IOI notifications
- `ioi-updates` - IOI status updates
- `ioi-expirations` - Expired IOI notifications

## Monitoring and Health Checks

The application includes Spring Boot Actuator endpoints:

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Performance metrics

## Testing

Run the test suite:

```bash
mvn test
```

## Performance Considerations

- **Matching Engine**: Multi-threaded processing with configurable thread pool
- **Order Book**: Concurrent data structures for high-performance updates
- **Database**: Optimized queries with proper indexing
- **Kafka**: Batch processing and async event handling

## Security Features

- Input validation and sanitization
- Risk management rules enforcement
- Audit logging for all operations
- Configurable access controls

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository or contact Devansh

## Roadmap

- [ ] WebSocket support for real-time updates
- [ ] Advanced order types (Stop Loss, Trailing Stop)
- [ ] Multi-currency support
- [ ] Advanced risk analytics
- [ ] Compliance reporting
- [ ] Performance optimization
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Monitoring and alerting
- [ ] Backtesting framework
=======
# Trading-Platform
A modern full-stack trading platform with real-time market data, portfolio management, and advanced trading features. Built with React/Next.js frontend and Spring Boot backend.
>>>>>>> origin/main
