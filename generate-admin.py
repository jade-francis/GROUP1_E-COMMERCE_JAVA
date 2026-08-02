#!/usr/bin/env python3
"""
generate-admin.py - Create an admin user with custom password
Usage: python3 generate-admin.py [email] [password] [name]
"""
import sys
import subprocess
import bcrypt

def generate_hash(password: str) -> str:
    """Generate BCrypt hash for password"""
    return bcrypt.hashpw(password.encode(), bcrypt.gensalt(10)).decode()

def main():
    email = sys.argv[1] if len(sys.argv) > 1 else "admin@test.com"
    password = sys.argv[2] if len(sys.argv) > 2 else "password123"
    name = sys.argv[3] if len(sys.argv) > 3 else "Admin User"

    # Generate BCrypt hash
    hash = generate_hash(password)
    print(f"Generated hash for '{password}': {hash}")

    # SQL to execute
    sql = f"""
    INSERT INTO users (name, email, password_hash, role, seller_status, created_at)
    VALUES ('{name}', '{email}', '{hash}', 'ADMIN', 'NOT_SELLER', NOW())
    ON CONFLICT (email) DO UPDATE SET 
        role = 'ADMIN',
        name = EXCLUDED.name,
        password_hash = EXCLUDED.password_hash;
    """

    # Execute via psql
    try:
        result = subprocess.run(
            ["sudo", "-u", "postgres", "psql", "-d", "shopease_db", "-c", sql],
            capture_output=True,
            text=True,
            check=True
        )
        print(result.stdout)
        
        # Verify
        verify_sql = f"SELECT id, email, role, seller_status FROM users WHERE email = '{email}';"
        result = subprocess.run(
            ["sudo", "-u", "postgres", "psql", "-d", "shopease_db", "-c", verify_sql],
            capture_output=True,
            text=True,
            check=True
        )
        print(result.stdout)
        print(f"\nAdmin user ready!")
        print(f"  Email:    {email}")
        print(f"  Password: {password}")
        
    except subprocess.CalledProcessError as e:
        print(f"Error: {e.stderr}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] in ("-h", "--help"):
        print("Usage: python3 generate-admin.py <email> [password] [name]")
        print("  email:    Admin email (default: admin@test.com)")
        print(f"  password: Password (default: password123)")
        print(f"  name:     Display name (default: Admin User)")
        sys.exit(0)
    main()