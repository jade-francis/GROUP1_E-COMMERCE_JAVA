# Admin User Creation Scripts

This project includes scripts to create admin users in the database.

## Quick Start

### Linux / WSL / macOS
```bash
# Use defaults (admin@test.com / password123)
./create-admin.sh

# Custom email/password/name
./create-admin.sh myadmin@domain.com mypassword "My Admin"
```

### Windows
```cmd
REM Use defaults
create-admin.bat

REM Custom
create-admin.bat myadmin@domain.com mypassword "My Admin"
```

### Python (Cross-platform, generates custom password hash)
```bash
# Install bcrypt first: pip install bcrypt
python3 generate-admin.py myadmin@domain.com mypassword "My Admin"
```

---

## Default Credentials

| Field | Value |
|-------|-------|
| Email | `admin@test.com` |
| Password | `password123` |
| Name | `Admin User` |
| Role | `ADMIN` |

---

## What the Scripts Do

1. **Check database exists** (`shopease_db`)
2. **Insert or update** user with `role = 'ADMIN'`
3. **Use BCrypt hash** compatible with Spring Security
4. **Verify creation** by querying the database

---

## Manual SQL (if you prefer)

```sql
-- Connect to database
sudo -u postgres psql -d shopease_db

-- Insert admin (password = "password123")
INSERT INTO users (name, email, password_hash, role, seller_status, created_at)
VALUES ('Admin User', 'admin@test.com', '$2a$10$d7Qnw91QyoksuONjD6y57uaE1sqrPqpQ0DVYVIPWAeVJFikIQ/Tyu', 'ADMIN', 'NOT_SELLER', NOW())
ON CONFLICT (email) DO UPDATE SET role = 'ADMIN';
```

---

## Requirements

- PostgreSQL running with `shopease_db` database
- Flyway migrations applied (`./mvnw flyway:migrate`)
- `psql` in PATH (or PostgreSQL bin directory added to PATH)

---

## After Creation

Login at: `http://localhost:8080/login`
- **Email:** your admin email
- **Password:** your password (default: `password123`)

Access admin panel at: `http://localhost:8080/admin/sellers`