# Microbank: A Full-Stack Microservice Banking Platform

## 🏦 Overview

Microbank is a production-ready, enterprise-grade banking platform built with modern microservices architecture. The platform provides secure banking operations with comprehensive client management, transaction processing, and administrative capabilities.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Client  │    │   Admin Panel   │    │   API Gateway   │
│   (Tailwind)    │    │   (Tailwind)    │    │   (Nginx)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                       │
                                └───────────────────────┼───────────────────────┐
                                                        │                       │
                                ┌───────────────────────┼───────────────────────┘
                                │                       │
                        ┌───────▼────────┐    ┌─────────▼─────────┐
                        │ Client Service │    │ Banking Service   │
                        │ (Spring Boot)  │    │ (Spring Boot)     │
                        └────────────────┘    └───────────────────┘
                                │                       │
                                └───────────────────────┼───────────────────────┐
                                                        │                       │
                                ┌───────────────────────┼───────────────────────┘
                                │                       │
                        ┌───────┴────────┐    ┌─────────┴─────────┐
                        │   PostgreSQL   │    │   Keycloak        │
                        │   Database     │    │   Identity Mgmt   │
                        └────────────────┘    └───────────────────┘
```

## 🚀 Features

### Core Banking Operations
- **Client Registration & Authentication** via JWT
- **Secure Banking Transactions** (deposits, withdrawals)
- **Real-time Balance Tracking**
- **Transaction History & Audit Trail**
- **Overdraft Protection**

### Security & Compliance
- **JWT-based Authentication** with Spring Security
- **Role-based Access Control** (Client, Admin)
- **Client Blacklisting System**
- **Rate Limiting & Security Headers**
- **Audit Logging**

### Administrative Features
- **Client Management Dashboard**
- **Blacklist Management**
- **Transaction Monitoring**
- **System Health Metrics**

## 🛠️ Tech Stack

### Backend Services
- **Java 17** with **Spring Boot 3.x**
- **Spring Security** with **JWT** authentication
- **Spring Data JPA** with **PostgreSQL**
- **Flyway** for database migrations
- **Spring Boot Actuator** for monitoring

### Frontend
- **React 18** with **TypeScript**
- **Tailwind CSS** for styling
- **React Router** for navigation
- **Axios** for API communication
- **React Query** for state management

### Infrastructure
- **PostgreSQL** for data persistence
- **Docker** for containerization
- **Nginx** as reverse proxy
- **Keycloak** for identity management (optional)

## 📁 Project Structure

```
microbank/
├── client/                          # React frontend application
│   ├── src/
│   │   ├── components/             # Reusable UI components
│   │   ├── pages/                  # Application pages
│   │   ├── contexts/               # React contexts
│   │   ├── services/               # API service layer
│   │   └── types/                  # TypeScript type definitions
│   ├── public/                     # Static assets
│   └── package.json
├── services/
│   ├── client-service/             # Client management service
│   │   ├── src/main/java/
│   │   ├── src/main/resources/
│   │   └── pom.xml
│   └── banking-service/            # Banking operations service
│       ├── src/main/java/
│       ├── src/main/resources/
│       └── pom.xml
├── infrastructure/                  # Infrastructure as code
│   ├── docker-compose.yml
│   ├── nginx/
│   └── postgres/
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 14+ (if running locally)

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd microbank
   ```

2. **Start all services**
   ```bash
   cd infrastructure
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Client Service API: http://localhost:8081/client-service
   - Banking Service API: http://localhost:8082/banking-service
   - Swagger UI: http://localhost:8081/client-service/swagger-ui.html

### Option 2: Local Development (Recommended for Development)

**📖 See the comprehensive guide: [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md)**

For quick local setup with your local PostgreSQL database:

1. **Ensure your local PostgreSQL is running**
   - Database: `microbank`
   - Username: `postgres`
   - Password: `password@001`

2. **Start Client Service**
   ```bash
   cd services/client-service
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. **Start Banking Service**
   ```bash
   cd services/banking-service
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Start API Gateway**
   ```bash
   cd services/api-gateway
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. **Start Frontend**
   ```bash
   cd client
   npm install
   npm start
   ```

## 🔧 Configuration

### Environment Variables

#### Client Service
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/microbank
SPRING_DATASOURCE_USERNAME=microbank
SPRING_DATASOURCE_PASSWORD=microbank
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

#### Banking Service
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/microbank
SPRING_DATASOURCE_USERNAME=microbank
SPRING_DATASOURCE_PASSWORD=microbank
CLIENT_SERVICE_URL=http://localhost:8081/client-service
```

## 📚 API Documentation

### Client Service API
- **POST** `/api/v1/clients/register` - Client registration
- **POST** `/api/v1/clients/login` - Client authentication
- **GET** `/api/v1/clients/profile` - Get client profile
- **PUT** `/api/v1/clients/{id}/blacklist` - Blacklist client (Admin only)

### Banking Service API
- **POST** `/api/v1/transactions/deposit` - Make deposit
- **POST** `/api/v1/transactions/withdraw` - Make withdrawal
- **GET** `/api/v1/accounts/{id}/balance` - Get account balance
- **GET** `/api/v1/transactions/history` - Get transaction history

## 🧪 Testing

### Backend Testing
```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# Test coverage
./mvnw jacoco:report
```

### Frontend Testing
```bash
# Unit tests
npm test

# Test coverage
npm run test:coverage
```

## 🚀 Deployment

### Docker Deployment
1. **Build images**
   ```bash
   docker build -t microbank-client ./client
   docker build -t microbank-client-service ./services/client-service
   docker build -t microbank-banking-service ./services/banking-service
   ```

2. **Deploy with Docker Compose**
   ```bash
   docker-compose up -d
   ```

### Production Considerations
- **Load Balancing** with Nginx
- **SSL/TLS** termination
- **Monitoring** with Spring Boot Actuator
- **Logging** with structured format
- **Health Checks** for all services

## 🔒 Security Features

- **JWT Token Validation** across all services
- **CORS Configuration** for frontend access
- **Rate Limiting** on sensitive endpoints
- **Input Validation** and sanitization
- **SQL Injection Prevention** with JPA
- **XSS Protection** headers

## 📊 Monitoring & Observability

- **Health Checks** via Spring Boot Actuator
- **Metrics** with Micrometer and Prometheus
- **Logging** with structured JSON format
- **API Documentation** with Swagger/OpenAPI

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Email: sharons@kenac.co.zw
- Copy: kudzaim@kenac.co.zw

## 🎯 Roadmap

- [x] **Phase 1**: Core banking operations ✅
- [x] **Phase 2**: Client management & authentication ✅
- [x] **Phase 3**: Admin panel & blacklisting ✅
- [ ] **Phase 4**: Advanced features (loans, investments)
- [ ] **Phase 5**: Mobile application
- [ ] **Phase 6**: AI-powered fraud detection
- [ ] **Phase 7**: Multi-currency support

---

**Built with ❤️ using Spring Boot, React, and Docker**
