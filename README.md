# Microbank Microservices Architecture

## 🏗️ Architecture Overview

The Microbank platform implements a clean microservices architecture with two core business services, an API Gateway for centralized routing, and a modern React frontend. This architecture ensures scalability, maintainability, and clear separation of concerns.

## 🎯 Core Services

### 1. Client Service
**Purpose**: Manages user accounts, authentication, and client lifecycle  
**Port**: 8081  
**Responsibilities**:
- User registration and authentication
- Profile management
- Client status management (active/blacklisted)
- JWT token generation and validation
- Admin operations for client management

**Key Components**:
- `ClientController` - REST API endpoints
- `ClientService` - Business logic layer
- `ClientRepository` - Data access layer
- `JwtTokenProvider` - Authentication utilities
- `SecurityConfig` - Spring Security configuration

### 2. Banking Service
**Purpose**: Handles all banking operations and financial transactions  
**Port**: 8082  
**Responsibilities**:
- Account balance management
- Deposit and withdrawal operations
- Transaction history and audit
- Overdraft prevention
- Integration with client blacklist validation

**Key Components**:
- `BankingController` - Banking API endpoints
- `AccountService` - Account management logic
- `TransactionService` - Transaction processing
- `AuditAspect` - Transaction auditing
- `ClientValidationService` - Client status validation

## 🌐 API Gateway

**Purpose**: Centralized entry point for all client requests  
**Port**: 8080  
**Responsibilities**:
- Request routing to appropriate services
- Load balancing and service discovery
- Authentication and authorization
- Rate limiting and security
- Request/response logging and monitoring

**Technology**: Direct service communication with load balancer

## 🗄️ Data Architecture

### Database Design
- **PostgreSQL** as the primary database
- **Flyway** for database migrations
- **Separate schemas** for each service
- **Shared database** for simplified deployment

### Data Flow
```
Client Request → API Gateway → Service → Database
                ↓
            Audit Logging
```

## 🔐 Security Architecture

### Authentication Flow
1. **Client Registration/Login** → Client Service
2. **JWT Token Generation** → Client Service
3. **Token Validation** → All Services
4. **Role-Based Access** → Service Level

### Security Features
- **JWT Tokens** with configurable expiration
- **Role-Based Access Control** (CLIENT, ADMIN)
- **Rate Limiting** on sensitive endpoints
- **Input Validation** and sanitization
- **CORS Configuration** for frontend access

## 📡 Service Communication

### Inter-Service Communication
- **Synchronous**: HTTP REST APIs
- **Authentication**: JWT token validation
- **Validation**: Client status checks before banking operations
- **Error Handling**: Comprehensive error responses

### Service Discovery
- **Static Configuration**: Service URLs in application properties
- **Health Checks**: Actuator endpoints for service monitoring
- **Load Balancing**: Application Load Balancer-based distribution

## 🚀 Deployment Architecture

### Container Strategy
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Client  │    │  Admin Panel    │    │  API Gateway    │
│   (Port 3000)   │    │   (Port 3000)   │    │   (Port 8080)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Client Service │    │ Banking Service │    │  PostgreSQL     │
│ (Port 8081)     │    │ (Port 8082)     │    │   (Port 5432)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Docker Services
- **client**: React frontend application
- **client-service**: Java Spring Boot client management
- **banking-service**: Java Spring Boot banking operations
- **postgres**: PostgreSQL database


## 📊 Monitoring and Observability

### Health Checks
- **Spring Boot Actuator** endpoints
- **Health indicators** for database connectivity
- **Service status** monitoring

### Logging Strategy
- **Structured logging** with consistent format
- **Audit logging** for all operations
- **Performance metrics** collection
- **Error tracking** and alerting

### Metrics Collection
- **Response times** for all endpoints
- **Error rates** and success percentages
- **Database performance** metrics
- **Service availability** monitoring

## 🔄 Scalability Considerations

### Horizontal Scaling
- **Stateless services** for easy replication
- **Database connection pooling** for performance
- **Load balancer** ready architecture
- **Container orchestration** support

### Performance Optimization
- **Database indexing** for query optimization
- **Caching strategies** for frequently accessed data
- **Async processing** for non-critical operations
- **Resource monitoring** and optimization

## 🧪 Testing Strategy

### Testing Levels
- **Unit Tests**: Service layer and business logic
- **Integration Tests**: API endpoints and database operations
- **End-to-End Tests**: Complete user workflows
- **Performance Tests**: Load and stress testing

### Test Coverage
- **Service Layer**: >90% coverage
- **Controller Layer**: >85% coverage
- **Repository Layer**: >80% coverage
- **Integration Tests**: All critical paths

## 🚧 Development Workflow

### Code Organization
- **Feature Branches** for new development
- **Pull Request Reviews** for code quality
- **Automated Testing** on all commits
- **Documentation Updates** with code changes

### Deployment Pipeline
- **Development**: Local Docker Compose setup
- **Staging**: Automated testing environment
- **Production**: Containerized deployment
- **Rollback**: Quick service rollback capability

## 📈 Future Enhancements

### Planned Improvements
- **Message Queue Integration**: Asynchronous processing
- **Service Mesh**: Advanced service communication
- **Multi-Region Deployment**: Geographic distribution
- **Advanced Monitoring**: APM and tracing

### Technology Upgrades
- **Spring Boot 3.x**: Latest features and security
- **Java 21**: Performance improvements
- **PostgreSQL 16**: Advanced database features
- **React 18**: Modern frontend capabilities

## 🎯 Architecture Benefits

### Maintainability
- **Clear separation** of concerns
- **Independent deployment** of services
- **Technology flexibility** for each service
- **Easy debugging** and troubleshooting

### Scalability
- **Horizontal scaling** of individual services
- **Load distribution** across service instances
- **Resource optimization** per service
- **Performance monitoring** and tuning

### Reliability
- **Service isolation** prevents cascading failures
- **Health monitoring** for proactive maintenance
- **Graceful degradation** during service issues
- **Comprehensive logging** for issue resolution

---

**This architecture provides a solid foundation for the Microbank platform, ensuring it meets all challenge requirements while maintaining scalability, security, and maintainability.**
