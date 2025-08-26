# React Trading Frontend Setup

## Prerequisites
You need to install Node.js first:

### Option 1: Install Node.js via Homebrew
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Node.js
brew install node

# Verify installation
node --version
npm --version
```

### Option 2: Download Node.js directly
1. Go to https://nodejs.org/
2. Download the LTS version for macOS
3. Install the .pkg file

## Setup Steps

1. **Navigate to the frontend directory:**
```bash
cd "/Users/devansh/Downloads/html_css_js_react copy/trading-frontend"
```

2. **Install dependencies:**
```bash
npm install
```

3. **Start the development server:**
```bash
npm run dev
```

4. **Start your Spring Boot backend (in another terminal):**
```bash
cd "/Users/devansh/Downloads/html_css_js_react copy"
mvn spring-boot:run
```

5. **Access the application:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

## What's Been Created

✅ **Modern React/Next.js Frontend** with:
- Professional UI using Payload CMS components
- Tailwind CSS styling
- TypeScript support
- Real-time portfolio calculations
- Order management system
- Authentication integration

✅ **API Integration** with your existing Spring Boot backend:
- All existing endpoints work (/api/auth/*, /api/orders, etc.)
- User authentication with tokens
- Data persistence across page reloads
- P&L calculations with market data updates

✅ **Features Maintained**:
- All your working trading functionality
- Portfolio holdings display
- Order history tracking
- Market data simulation
- User-specific data isolation

## Architecture
- **Frontend**: Next.js 14 + React + Tailwind (Port 3000)
- **Backend**: Spring Boot + PostgreSQL (Port 8080)
- **Communication**: REST API calls with JWT authentication

The new React frontend provides the same functionality as your HTML dashboard but with modern, professional UI components and better maintainability.
