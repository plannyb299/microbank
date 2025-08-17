# 🚀 Local Development Guide

## Overview
This guide explains how to run all Microbank services locally using your local PostgreSQL database.

## 🗄️ **Prerequisites**

### **Local PostgreSQL Database**
- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `microbank`
- **Username**: `postgres`
- **Password**: `password@001`

### **Required Software**
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+ (local installation)

## 🏗️ **Service Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │  Microservices  │
│   (Port 3000)   │◄──►│   (Port 8083)   │◄──►│                 │
└─────────────────┘    └─────────────────┘    │  • Client       │
                                              │    (Port 8081)  │
                                              │  • Banking      │
                                              │    (Port 8082)  │
                                              └─────────────────┘
```

## 🚀 **Starting Services Locally**

### **1. Start PostgreSQL Database**
Ensure your local PostgreSQL is running and accessible:
```bash
# Test connection
psql -h localhost -U postgres -d microbank
# Enter password: password@001
```

### **2. Start Client Service (Port 8081)**
```bash
cd services/client-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Health Check**: `http://localhost:8081/actuator/health`

### **3. Start Banking Service (Port 8082)**
```bash
cd services/banking-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Health Check**: `http://localhost:8082/actuator/health`

### **4. Start API Gateway (Port 8083)**
```bash
cd services/api-gateway
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Health Check**: `http://localhost:8083/health`

### **5. Start Frontend (Port 3000)**
```bash
cd client
npm install
npm start
```

**Access**: `http://localhost:3000`

## 🔧 **Service Configuration**

### **Local Profile Files**
Each service has an `application-local.yml` file configured for local development:

- **`services/client-service/src/main/resources/application-local.yml`**
- **`services/banking-service/src/main/resources/application-local.yml`**
- **`services/api-gateway/src/main/resources/application-local.yml`**

### **Database Configuration**
All services use the same local PostgreSQL configuration:
```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/microbank
  username: postgres
  password: password@001
  driver-class-name: org.postgresql.Driver
```

## 📊 **API Endpoints**

### **API Gateway (Port 8083)**
- **Health**: `http://localhost:8083/health`
- **Root**: `http://localhost:8083/`
- **Client Service**: `http://localhost:8083/api/v1/clients/**`
- **Banking Service**: `http://localhost:8083/api/v1/banking/**`
- **Audit Service**: `http://localhost:8083/api/v1/audit/**`

### **Direct Service Access**
- **Client Service**: `http://localhost:8081/api/v1/clients/**`
- **Banking Service**: `http://localhost:8082/api/v1/banking/**`

## 🧪 **Testing the Setup**

### **1. Test Database Connection**
```bash
# Test each service can connect to database
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/health
```

### **2. Test API Gateway Routing**
```bash
# Test routing to Client Service
curl http://localhost:8083/api/v1/clients/health

# Test routing to Banking Service
curl http://localhost:8083/api/v1/banking/health
```

### **3. Test Frontend**
- Open `http://localhost:3000`
- Register a new user
- Test login functionality
- Access admin panel

## 🐛 **Troubleshooting**

### **Common Issues**

#### **1. Port Already in Use**
```bash
# Check what's using the port
netstat -ano | findstr :8081
netstat -ano | findstr :8082
netstat -ano | findstr :8083

# Kill the process
taskkill /PID <PID> /F
```

#### **2. Database Connection Failed**
- Ensure PostgreSQL is running on port 5432
- Verify database `microbank` exists
- Check credentials: `postgres` / `password@001`
- Test connection: `psql -h localhost -U postgres -d microbank`

#### **3. Service Discovery Issues**
- Start services in order: PostgreSQL → Client → Banking → Gateway → Frontend
- Ensure all services are healthy before starting the next one

#### **4. Flyway Migration Errors**
- Check if database schema exists
- Verify Flyway migration files are in `src/main/resources/db/migration/`
- Check database permissions for the `postgres` user

### **Logs and Debugging**
```bash
# View service logs in real-time
# Each service will show logs in its terminal window

# Check database tables
psql -h localhost -U postgres -d microbank -c "\dt"

# Check Flyway schema version
psql -h localhost -U postgres -d microbank -c "SELECT * FROM flyway_schema_history;"
```

## 🔄 **Development Workflow**

### **1. Start Development Environment**
```bash
# Terminal 1: Start PostgreSQL (if not running as service)
# Terminal 2: Start Client Service
cd services/client-service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 3: Start Banking Service  
cd services/banking-service && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 4: Start API Gateway
cd services/api-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 5: Start Frontend
cd client && npm start
```

### **2. Make Code Changes**
- Edit service code
- Services will auto-reload (if using `spring-boot-devtools`)
- Test changes via API endpoints or frontend

### **3. Database Changes**
- Create new Flyway migration files in `src/main/resources/db/migration/`
- Restart the service to apply migrations
- Test database changes

## 📁 **File Structure**
```
microbank/
├── services/
│   ├── client-service/
│   │   └── src/main/resources/
│   │       ├── application.yml          # Default config
│   │       └── application-local.yml    # Local development
│   ├── banking-service/
│   │   └── src/main/resources/
│   │       ├── application.yml          # Default config
│   │       └── application-local.yml    # Local development
│   └── api-gateway/
│       └── src/main/resources/
│           ├── application.yml          # Default config
│           └── application-local.yml    # Local development
├── client/                              # React frontend
└── LOCAL_DEVELOPMENT.md                 # This file
```

## 🎯 **Next Steps**

### **Immediate Testing**
1. ✅ Start all services locally
2. ✅ Test database connectivity
3. ✅ Test API Gateway routing
4. ✅ Test frontend functionality

### **Future Enhancements**
- Add Redis for rate limiting
- Implement centralized audit logging
- Add monitoring with Prometheus/Grafana
- Set up local SQS for async messaging

## 🆘 **Need Help?**

### **Check Service Status**
```bash
# All services should show "UP" status
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health  
curl http://localhost:8083/health
```

### **Common Commands**
```bash
# Restart a service
# Stop with Ctrl+C, then restart with mvn spring-boot:run

# Check database
psql -h localhost -U postgres -d microbank

# View service logs
# Check the terminal where each service is running
```

---

**Happy Coding! 🚀** Your local Microbank development environment is now ready!
