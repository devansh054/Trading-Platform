# PostgreSQL Setup Guide for Trading Platform

This guide will help you connect your Spring Boot trading platform application to PostgreSQL.

## Prerequisites

- Docker and Docker Compose installed
- Java 17 or higher
- Maven

## Quick Start

### 1. Start PostgreSQL Database

```bash
# Start PostgreSQL using Docker Compose
docker-compose up -d postgres

# Check if PostgreSQL is running
docker-compose ps
```

### 2. Verify Database Connection

The PostgreSQL database will be available at:
- **Host**: localhost
- **Port**: 5432
- **Database**: trading_db
- **Username**: postgres
- **Password**: postgres

### 3. Run the Application

```bash
# Run with local profile (uses PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or run the JAR file
mvn clean package
java -jar target/mini-equities-trading-platform-1.0.0.jar --spring.profiles.active=local
```

## Database Schema

The application will automatically create the following tables:
- `orders` - Trading orders
- `trades` - Executed trades
- `indications_of_interest` - IOI messages

## Configuration Options

### Environment Variables

You can override the default database configuration using environment variables:

```bash
export DB_HOST=your-postgres-host
export DB_PORT=5432
export DB_NAME=your-database-name
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export DDL_AUTO=update
```

### Profile-based Configuration

- **Default profile**: Uses environment variables with fallbacks
- **Local profile**: Uses hardcoded localhost configuration
- **Production profile**: Should be configured with production database credentials

## Database Management

### Using pgAdmin (Optional)

If you want to use pgAdmin for database management:

```bash
# Start pgAdmin along with PostgreSQL
docker-compose up -d

# Access pgAdmin at: http://localhost:8081
# Login: admin@trading.com / admin
```

### Direct Database Access

```bash
# Connect to PostgreSQL using psql
docker exec -it trading-postgres psql -U postgres -d trading_db

# List tables
\dt

# View table structure
\d orders
```

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Ensure PostgreSQL container is running: `docker-compose ps`
   - Check if port 5432 is available: `netstat -an | grep 5432`

2. **Authentication Failed**
   - Verify username/password in application.yml
   - Check if database exists: `docker exec -it trading-postgres psql -U postgres -l`

3. **Schema Creation Issues**
   - Ensure the postgres user has sufficient privileges
   - Check application logs for detailed error messages

### Logs

```bash
# View PostgreSQL logs
docker-compose logs postgres

# View application logs
docker-compose logs -f
```

## Performance Tuning

The configuration includes several PostgreSQL-specific optimizations:

- **Batch processing**: `jdbc.batch_size=20`
- **Ordered inserts/updates**: Better performance for bulk operations
- **Proper indexing**: Indexes on frequently queried columns
- **Connection pooling**: Managed by Spring Boot's default HikariCP

## Migration from H2

If you were previously using H2:

1. The application will automatically create the PostgreSQL schema
2. Data will need to be migrated manually if you have existing data
3. Update any H2-specific SQL queries to be PostgreSQL compatible

## Security Notes

- Change default passwords in production
- Use environment variables for sensitive configuration
- Consider using connection pooling for production deployments
- Implement proper database user roles and permissions
