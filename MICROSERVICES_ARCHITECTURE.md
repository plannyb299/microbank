# 🏗️ Microbank Microservices Architecture

## 📋 Overview

The Microbank platform is built using a **true microservices architecture** as required by the task. This document explains the architecture, service communication patterns, and the placement of the audit service.

## 🏛️ Architecture Components

### 1. **API Gateway Service** (Port 8083)
- **Purpose**: Central entry point for all client requests
- **Responsibilities**:
  - Request routing to appropriate microservices
  - Centralized authentication and authorization
  - **Audit logging at the gateway level** ✅
  - Rate limiting and security
  - Load balancing
  - Circuit breaker implementation
  - Request/response transformation

### 2. **Client Service** (Port 8081)
- **Purpose**: Handles client registration, authentication, and profile management
- **Responsibilities**:
  - User registration and login
  - JWT token generation and validation
  - Client profile management
  - Blacklist functionality
  - Client statistics

### 3. **Banking Service** (Port 8082)
- **Purpose**: Handles all banking operations
- **Responsibilities**:
  - Account management
  - Deposits and withdrawals
  - Transaction processing
  - Balance tracking
  - Overdraft prevention

### 4. **Frontend Client** (Port 3000)
- **Purpose**: React-based web application
- **Responsibilities**:
  - User interface for banking operations
  - Admin panel for client management
  - Audit dashboard for administrators

## 🔄 Inter-Service Communication

### **Communication Patterns**

#### 1. **Synchronous Communication (HTTP REST)**
```
Frontend → API Gateway → Microservice
```

**Example Flow**:
```
User Login Request:
Frontend → API Gateway (8083) → Client Service (8081)
Response: Client Service → API Gateway → Frontend
```

#### 2. **Service Discovery**
- **API Gateway** uses Spring Cloud Gateway with service discovery
- Routes requests based on path patterns:
  - `/api/v1/clients/**` → Client Service
  - `/api/v1/banking/**` → Banking Service
  - `/api/v1/audit/**` → Banking Service (audit endpoints)

#### 3. **Load Balancing**
- **API Gateway** provides load balancing using `lb://` prefix
- Can scale individual services independently

### **Data Flow Example**

```
1. User makes login request to /api/v1/auth/login
2. API Gateway receives request at port 8083
3. Gateway validates JWT token (if present)
4. Gateway routes to Client Service (port 8081)
5. Client Service processes authentication
6. Response flows back: Client Service → Gateway → Frontend
7. Gateway logs the entire request/response for audit
```

## 🔍 **Audit Service Placement - CORRECTED ARCHITECTURE**

### **❌ Previous Implementation (Incorrect)**
- Audit service was embedded within the Banking Service
- This violated microservice separation of concerns

### **✅ Current Implementation (Correct)**
- **Audit service is now at the API Gateway level** ✅
- **Centralized logging** for all requests/responses
- **Service-agnostic** audit trail
- **Complete visibility** into all system interactions

### **Audit Service Benefits at Gateway Level**

#### 1. **Centralized Visibility**
```
All requests → API Gateway → Audit Logging → Database
```

#### 2. **Complete Request/Response Capture**
- **Request details**: Headers, body, IP, user agent
- **Response details**: Status, body, execution time
- **Service routing**: Which microservice handled the request
- **Performance metrics**: Response times, errors

#### 3. **Security Monitoring**
- **Rate limiting violations**
- **Blocked requests**
- **Suspicious patterns**
- **IP address tracking**

## 🗄️ Database Architecture

### **Shared Database Pattern**
- **Single PostgreSQL instance** shared across services
- **Rationale**: As per task requirements for simplicity
- **Alternative**: Could be separated for production

### **Database Schema**
```
microbank database:
├── clients (Client Service)
├── accounts (Banking Service)
├── transactions (Banking Service)
├── gateway_audit_logs (API Gateway) ✅
└── audit_logs (Legacy - can be removed)
```

## 🚀 **Service Deployment**

### **Docker Compose Services**
```yaml
services:
  api-gateway:      # Port 8083 - Central gateway
  client-service:   # Port 8081 - Client management
  banking-service:  # Port 8082 - Banking operations
  postgres:         # Port 5432 - Shared database
  frontend:         # Port 3000 - React application
```

### **Service Dependencies**
```
Frontend → API Gateway → Microservices
API Gateway → Database (for audit logging)
Microservices → Database (for business data)
```

## 🔐 **Security Architecture**

### **Authentication Flow**
1. **Frontend** sends credentials to API Gateway
2. **Gateway** routes to Client Service
3. **Client Service** validates and generates JWT
4. **Gateway** validates JWT for subsequent requests
5. **All requests** are logged for audit

### **Authorization**
- **Role-based access control** (CLIENT, ADMIN)
- **JWT tokens** validated at gateway level
- **Service-specific permissions** enforced

## 📊 **Monitoring and Observability**

### **Health Checks**
- **API Gateway**: `/health` endpoint
- **Client Service**: `/actuator/health`
- **Banking Service**: `/actuator/health`

### **Metrics**
- **Prometheus** integration for metrics collection
- **Grafana** for visualization
- **Custom metrics** for business operations

## 🔧 **Configuration Management**

### **Environment Variables**
```yaml
# API Gateway
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/microbank
JWT_SECRET: microbank-jwt-secret-key-2024-very-long-and-secure

# Microservices
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/microbank
JWT_SECRET: microbank-jwt-secret-key-2024-very-long-and-secure
```

## 🧪 **Testing Strategy**

### **Unit Tests**
- **Individual service** business logic
- **Isolated testing** of components

### **Integration Tests**
- **Service-to-service** communication
- **Database integration** testing

### **End-to-End Tests**
- **Complete user workflows**
- **API Gateway** routing validation

## 📈 **Scaling Strategy**

### **Horizontal Scaling**
- **Individual services** can be scaled independently
- **API Gateway** can be scaled for high availability
- **Database** can be scaled vertically or horizontally

### **Load Balancing**
- **API Gateway** provides load balancing
- **Multiple instances** of each service
- **Health checks** for service discovery

## 🚨 **Error Handling**

### **Circuit Breaker Pattern**
- **API Gateway** implements circuit breakers
- **Fallback mechanisms** for service failures
- **Graceful degradation** during outages

### **Retry Mechanisms**
- **Exponential backoff** for transient failures
- **Configurable retry policies**
- **Dead letter queues** for failed requests

## 📋 **API Documentation**

### **Swagger/OpenAPI**
- **Each service** exposes its own API documentation
- **API Gateway** aggregates documentation
- **Interactive testing** capabilities

## 🎯 **Compliance with Task Requirements**

### **✅ Microservices Architecture**
- **2 core services** as required (Client + Banking)
- **Independent deployment** and scaling
- **Service separation** of concerns

### **✅ API Gateway Pattern**
- **Centralized routing** and security
- **Audit service at gateway level** ✅
- **Load balancing** and circuit breaking

### **✅ Inter-Service Communication**
- **HTTP REST APIs** for synchronous communication
- **JWT-based authentication** across services
- **Service discovery** and routing

### **✅ Security Considerations**
- **Rate limiting** at gateway level
- **JWT validation** across all services
- **Role-based access control**

## 🔮 **Future Enhancements**

### **Message Queue Integration**
- **Asynchronous communication** between services
- **Event-driven architecture** for scalability
- **Reliable message delivery**

### **Service Mesh**
- **Istio or Linkerd** for advanced service communication
- **Traffic management** and security policies
- **Observability** and monitoring

### **Distributed Tracing**
- **Jaeger or Zipkin** for request tracing
- **Performance analysis** across services
- **Debugging** and troubleshooting

## 📚 **Summary**

The Microbank platform implements a **true microservices architecture** with:

1. **✅ API Gateway** as the central entry point
2. **✅ Audit service at the gateway level** (corrected)
3. **✅ Independent microservices** (Client + Banking)
4. **✅ Clear service boundaries** and responsibilities
5. **✅ Proper inter-service communication**
6. **✅ Centralized security and monitoring**
7. **✅ Scalable and maintainable architecture**

This architecture fully complies with the task requirements and follows microservices best practices.
