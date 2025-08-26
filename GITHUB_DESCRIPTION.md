# 📈 Trading Platform

A comprehensive full-stack trading application featuring real-time market data, portfolio management, and advanced trading capabilities. Built with modern technologies for scalability and performance.

## 🚀 Features

### Core Trading Features
- **Real-time Order Management** - Market, Limit, and Advanced order types
- **Portfolio Tracking** - Live P&L calculations and holdings display
- **IOI Management** - Indication of Interest creation and tracking
- **Trade Messages** - XML-based trade message processing
- **Market Data Simulation** - Real-time price updates and charts

### User Management
- **Secure Authentication** - JWT-based user authentication
- **User-Specific Data** - Isolated data per user account
- **Account Management** - Sign up, login, and session management

### Advanced Features
- **XML Processing** - Trade message parsing and validation
- **Data Persistence** - PostgreSQL database integration
- **Real-time Updates** - Live data synchronization
- **Responsive UI** - Modern React interface with animations

## 🛠️ Tech Stack

### Frontend
- **React 18** with Next.js 14
- **TypeScript** for type safety
- **Tailwind CSS** for styling
- **Framer Motion** for animations
- **Chart.js** for data visualization
- **Axios** for API communication

### Backend
- **Spring Boot 3** with Java
- **Spring Security** for authentication
- **JPA/Hibernate** for ORM
- **PostgreSQL** database
- **Maven** for dependency management

### Infrastructure
- **Docker** support
- **Git** version control
- **Environment-based configuration**

## 📦 Project Structure

```
├── src/main/java/          # Spring Boot backend
├── trading-frontend/       # React/Next.js frontend
├── src/main/resources/     # Backend resources
├── docker-compose.yml      # Docker configuration
├── pom.xml                # Maven dependencies
└── README.md              # Documentation
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 12+
- Maven 3.6+

### Backend Setup
```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/trading-platform.git
cd trading-platform

# Start PostgreSQL database
# Update application-local.yml with your database credentials

# Run Spring Boot application
mvn clean install
mvn spring-boot:run
```

### Frontend Setup
```bash
# Navigate to frontend directory
cd trading-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

### Access Application
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Test Credentials:** `testuser` / `password`

## 🔧 Configuration

### Database Configuration
Update `src/main/resources/application-local.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trading_db
    username: postgres
    password: your_password
```

### Environment Variables
```bash
# Frontend (.env.local)
NEXT_PUBLIC_API_URL=http://localhost:8080

# Backend
SPRING_PROFILES_ACTIVE=local
```

## 📊 API Endpoints

### Authentication
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration

### Trading
- `GET /api/orders` - Get user orders
- `POST /api/orders` - Create new order
- `GET /api/portfolio` - Get portfolio data

### IOI Management
- `GET /api/ioi/active` - Get active IOIs
- `POST /api/ioi` - Create new IOI

## 🎨 UI Features

- **Animated Login** - Beautiful circle animations
- **Dark Theme** - Professional trading interface
- **Responsive Design** - Works on all devices
- **Real-time Charts** - Live market data visualization
- **Custom Logo Support** - Branded experience

## 🚀 Deployment

### Frontend (Vercel)
```bash
npm run build
# Deploy to Vercel
```

### Backend (Railway/Heroku)
```bash
mvn clean package
# Deploy JAR file
```

### Database (Railway/Supabase)
- PostgreSQL hosting with connection string

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the powerful frontend library
- Chart.js for data visualization capabilities

---

**Built with ❤️ for modern trading applications**
