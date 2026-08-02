@echo off
REM create-admin.bat - Create an admin user in the ShopEase database (Windows)
REM Usage: create-admin.bat [email] [password] [name]
REM Defaults: admin@test.com / password123 / Admin User

setlocal enabledelayedexpansion

set EMAIL=%1
set PASSWORD=%2
set NAME=%3

if "%EMAIL%"=="" set EMAIL=admin@test.com
if "%PASSWORD%"=="" set PASSWORD=password123
if "%NAME%"=="" set NAME=Admin User

REM BCrypt hash for "password123" (10 rounds)
set BCRYPT_HASH=$2a$10$d7Qnw91QyoksuONjD6y57uaE1sqrPqpQ0DVYVIPWAeVJFikIQ/Tyu

echo Creating admin user...
echo   Email: %EMAIL%
echo   Name:  %NAME%

REM Check if psql is available
where psql >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: psql not found in PATH. Add PostgreSQL bin to PATH.
    echo Example: set PATH=%PATH%;C:\Program Files\PostgreSQL\16\bin
    exit /b 1
)

REM Insert or update admin user
psql -U postgres -d shopease_db -c "INSERT INTO users (name, email, password_hash, role, seller_status, created_at) VALUES ('%NAME%', '%EMAIL%', '%BCRYPT_HASH%', 'ADMIN', 'NOT_SELLER', NOW()) ON CONFLICT (email) DO UPDATE SET role = 'ADMIN', name = EXCLUDED.name, password_hash = EXCLUDED.password_hash;"

REM Verify
psql -U postgres -d shopease_db -c "SELECT id, email, role, seller_status FROM users WHERE email = '%EMAIL%';"

echo.
echo Admin user ready! Login at http://localhost:8080/login
echo   Email:    %EMAIL%
echo   Password: %PASSWORD%