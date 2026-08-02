#!/bin/bash
# create-admin.sh - Create an admin user in the ShopEase database
# Usage: ./create-admin.sh [email] [password] [name]
# Defaults: admin@test.com / password123 / Admin User

set -e

EMAIL="${1:-admin@test.com}"
PASSWORD="${2:-password123}"
NAME="${3:-Admin User}"

# BCrypt hash for "password123" (10 rounds)
# To generate a new hash: use BCrypt generator or run: python3 -c "import bcrypt; print(bcrypt.hashpw(b'password123', bcrypt.gensalt(10)).decode())"
BCRYPT_HASH='$2a$10$d7Qnw91QyoksuONjD6y57uaE1sqrPqpQ0DVYVIPWAeVJFikIQ/Tyu'

echo "Creating admin user..."
echo "  Email: $EMAIL"
echo "  Name:  $NAME"

# Check if PostgreSQL is running and database exists
if ! sudo -u postgres psql -lqt | cut -d \| -f 1 | grep -qw shopease_db; then
    echo "Error: Database 'shopease_db' not found. Run Flyway migrations first:"
    echo "  ./mvnw flyway:migrate"
    exit 1
fi

# Insert or update admin user
sudo -u postgres psql -d shopease_db -c "
INSERT INTO users (name, email, password_hash, role, seller_status, created_at)
VALUES ('$NAME', '$EMAIL', '$BCRYPT_HASH', 'ADMIN', 'NOT_SELLER', NOW())
ON CONFLICT (email) DO UPDATE SET 
    role = 'ADMIN',
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash;
"

# Verify
sudo -u postgres psql -d shopease_db -c "SELECT id, email, role, seller_status FROM users WHERE email = '$EMAIL';"

echo ""
echo "Admin user ready! Login at http://localhost:8080/login"
echo "  Email:    $EMAIL"
echo "  Password: $PASSWORD"