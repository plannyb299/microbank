# API Gateway Service

## Overview
The API Gateway service acts as the central entry point for all client requests in the Microbank platform. It provides routing, load balancing, rate limiting, and centralized audit logging.

## Port Configuration
- **Local Development**: Port 8083
- **Docker**: Port 8083 (mapped from host)

## Running Locally

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Local Development (Local PostgreSQL Database)
```bash
# Navigate to the API Gateway directory
cd services/api-gateway

# Run with local profile (uses local PostgreSQL database)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Prerequisites for Local Development:**
- Local PostgreSQL database running on port 5432
- Database named `microbank`
- User `postgres` with password `password@001`

The service will start on `http://localhost:8083`

### Health Check
- **Health Endpoint**: `http://localhost:8083/health`
- **Root Endpoint**: `http://localhost:8083/`

## Running with Docker

### Using Docker Compose (Recommended)
```bash
# From the project root
docker-compose up api-gateway
```

### Standalone Docker
```bash
# Build the image
docker build -t microbank-api-gateway .

# Run the container
docker run -p 8083:8083 microbank-api-gateway
```

## Configuration

### Profiles
- **`local`**: Uses local PostgreSQL database (localhost:5432)
- **`docker`**: Uses PostgreSQL database in Docker container

### Environment Variables
```yaml
# Database (Docker profile)
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/microbank
SPRING_DATASOURCE_USERNAME: microbank
SPRING_DATASOURCE_PASSWORD: microbank123

# JWT
JWT_SECRET: microbank-jwt-secret-key-2024-very-long-and-secure
JWT_EXPIRATION: 86400000
```

## API Routes

### Client Service
- **Path**: `/api/v1/clients/**`
- **Target**: Client Service (Port 8081)
- **Rate Limit**: 10 requests/minute

### Banking Service
- **Path**: `/api/v1/banking/**`
- **Target**: Banking Service (Port 8082)
- **Rate Limit**: 20 requests/minute

### Audit Service
- **Path**: `/api/v1/audit/**`
- **Target**: Banking Service (Port 8082)
- **Rate Limit**: 5 requests/minute

## Features

### ✅ Implemented
- **Spring Cloud Gateway** routing
- **Rate Limiting** with configurable limits
- **Circuit Breaker** pattern
- **Health Checks** and monitoring
- **CORS** configuration
- **Security Headers**

### 🔄 In Progress
- **Centralized Audit Logging** (database integration)
- **JWT Authentication** at gateway level
- **Request/Response Transformation**

## Troubleshooting

### Common Issues

#### 1. Port Already in Use
```bash
# Check what's using port 8083
netstat -ano | findstr :8083

# Kill the process
taskkill /PID <PID> /F
```

#### 2. Database Connection Failed
- **Local Development**: Ensure local PostgreSQL is running and accessible
- **Docker**: Ensure PostgreSQL service is running

#### 3. Service Discovery Issues
- **Local Development**: Services must be running on expected ports
- **Docker**: Use Docker Compose for proper service discovery

### Logs
```bash
# View logs
docker-compose logs api-gateway

# Follow logs
docker-compose logs -f api-gateway
```

## Development

### Adding New Routes
1. Update `application.yml` or `application-local.yml`
2. Add route configuration with predicates and filters
3. Test the route locally

### Adding New Filters
1. Implement custom filter logic
2. Configure in route definitions
3. Test with appropriate endpoints

## Monitoring

### Health Endpoints
- **Health**: `/health` - Service health status
- **Info**: `/actuator/info` - Service information
- **Metrics**: `/actuator/metrics` - Performance metrics

### Prometheus Integration
- **Metrics**: `/actuator/prometheus` - Prometheus format metrics
- **Configuration**: Available in Docker Compose monitoring profile

## Security

### Rate Limiting
- **Default**: 60 requests/minute
- **Authentication**: 10 requests/minute
- **Banking**: 100 requests/minute
- **Admin**: 30 requests/minute

### Security Headers
- **X-Frame-Options**: SAMEORIGIN
- **X-Content-Type-Options**: nosniff
- **X-XSS-Protection**: 1; mode=block
- **Referrer-Policy**: strict-origin-when-cross-origin
