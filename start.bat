@echo off
echo 🚀 Starting Microbank Platform...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker and try again.
    pause
    exit /b 1
)

echo 📦 Building and starting services...
cd infrastructure

REM Build and start all services
docker-compose up -d --build

echo ⏳ Waiting for services to start...
timeout /t 30 /nobreak >nul

echo 🔍 Checking service health...

REM Check PostgreSQL
docker-compose exec -T postgres pg_isready -U microbank >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ PostgreSQL is ready
) else (
    echo ❌ PostgreSQL is not ready
)

REM Check Client Service
curl -f http://localhost:8081/client-service/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Client Service is ready
) else (
    echo ❌ Client Service is not ready
)

REM Check Banking Service
curl -f http://localhost:8082/banking-service/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Banking Service is ready
) else (
    echo ❌ Banking Service is not ready
)

REM Check Frontend
curl -f http://localhost:3000 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Frontend is ready
) else (
    echo ❌ Frontend is not ready
)

echo.
echo 🎉 Microbank Platform is starting up!
echo.
echo 📱 Frontend: http://localhost:3000
echo 🔧 Client Service API: http://localhost:8081/client-service
echo 🏦 Banking Service API: http://localhost:8082/banking-service
echo 📚 Swagger UI: http://localhost:8081/client-service/swagger-ui.html
echo 🔐 Keycloak: http://localhost:8080 (admin/admin)
echo.
echo 📊 Monitor services: docker-compose logs -f
echo 🛑 Stop services: docker-compose down
echo.
echo 💡 Default admin credentials: admin@microbank.com / admin123
echo.
pause
