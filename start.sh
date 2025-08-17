#!/bin/bash

echo "🚀 Starting Microbank Platform..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

echo "📦 Building and starting services..."
cd infrastructure

# Build and start all services
docker-compose up -d --build

echo "⏳ Waiting for services to start..."
sleep 30

# Check service health
echo "🔍 Checking service health..."

# Check PostgreSQL
if docker-compose exec -T postgres pg_isready -U microbank > /dev/null 2>&1; then
    echo "✅ PostgreSQL is ready"
else
    echo "❌ PostgreSQL is not ready"
fi

# Check Client Service
if curl -f http://localhost:8081/client-service/actuator/health > /dev/null 2>&1; then
    echo "✅ Client Service is ready"
else
    echo "❌ Client Service is not ready"
fi

# Check Banking Service
if curl -f http://localhost:8082/banking-service/actuator/health > /dev/null 2>&1; then
    echo "✅ Banking Service is ready"
else
    echo "❌ Banking Service is not ready"
fi

# Check Frontend
if curl -f http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Frontend is ready"
else
    echo "❌ Frontend is not ready"
fi

echo ""
echo "🎉 Microbank Platform is starting up!"
echo ""
echo "📱 Frontend: http://localhost:3000"
echo "🔧 Client Service API: http://localhost:8081/client-service"
echo "🏦 Banking Service API: http://localhost:8082/banking-service"
echo "📚 Swagger UI: http://localhost:8081/client-service/swagger-ui.html"
echo "🔐 Keycloak: http://localhost:8080 (admin/admin)"
echo ""
echo "📊 Monitor services: docker-compose logs -f"
echo "🛑 Stop services: docker-compose down"
echo ""
echo "💡 Default admin credentials: admin@microbank.com / admin123"
