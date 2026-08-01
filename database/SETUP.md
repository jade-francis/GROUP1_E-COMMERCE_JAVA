# ShopEase Database — Setup Guide

This guide takes you from a fresh machine to a working ShopEase database
that a Java program can talk to. No prior context needed. Written for
PostgreSQL 15+ (developed on PostgreSQL 17).

---

## What you're setting up

- A PostgreSQL database called **`shopease`**
- Owned by a database role called **`shopease_admin`**
- Six tables (users, categories, products, cart_items, orders, order_items)
- A small Java layer that reads/writes products, with a test you can run

---

## 1. Install PostgreSQL

**Debian / Ubuntu / Parrot / Kali:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**macOS (Homebrew):**
```bash
brew install postgresql@17
brew services start postgresql@17
```

Confirm it's running:
```bash
psql --version
```

---

## 2. Install a JDK (to compile/run the Java)

You need Java 8 or newer.
```bash
sudo apt install default-jdk     # Debian-based
java -version
javac -version
```

The PostgreSQL JDBC driver (`postgresql-42.7.3.jar`) is already included
in this project folder, so you don't need to download it separately.

---

## 3. Create the role and database

Run these as the `postgres` superuser. On Linux, `sudo -u postgres psql`
gets you a superuser prompt without needing a Postgres password.

```bash
sudo -u postgres psql
```

Then, at the `postgres=#` prompt, paste:
```sql
-- create the login role the app will use
CREATE ROLE shopease_admin LOGIN PASSWORD 'shopease123';

-- create the database and hand ownership to that role
CREATE DATABASE shopease OWNER shopease_admin;

\q
```

> **Important:** `shopease_admin` must OWN the database. On PostgreSQL 15+,
> only the database owner can create tables in the default `public` schema.
> If the DB already exists but is owned by `postgres`, fix it with:
> `sudo -u postgres psql -c "ALTER DATABASE shopease OWNER TO shopease_admin;"`

Verify the role can create tables:
```bash
PGPASSWORD=shopease123 psql -h localhost -U shopease_admin -d shopease \
  -c "CREATE TABLE _t(x int); DROP TABLE _t;"
```
You should see `CREATE TABLE` / `DROP TABLE` with no permission error.

---

## 4. Build the tables (run schema.sql)

From inside this project folder:
```bash
PGPASSWORD=shopease123 psql -h localhost -U shopease_admin -d shopease -f schema.sql
```

Confirm the six tables exist and the seed data loaded:
```bash
PGPASSWORD=shopease123 psql -h localhost -U shopease_admin -d shopease -c "\dt"
PGPASSWORD=shopease123 psql -h localhost -U shopease_admin -d shopease -c "SELECT * FROM products;"
```

> **Note:** `schema.sql` begins with `DROP TABLE IF EXISTS ...`. Re-running it
> **wipes all data and recreates the tables from scratch.** That's convenient
> during development but destructive — don't run it against real data.

---

## 5. Configure the database credentials

The Java code does **not** contain any password. It reads the connection
details at runtime from a file called `db.properties`, which is gitignored
so it never gets committed.

Copy the template and fill in your values:
```bash
cp db.properties.example db.properties
```

Then edit `db.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/shopease
db.user=shopease_admin
db.password=shopease123
```

> **Why this matters:** hardcoded secrets in source code are one of the most
> common real-world security mistakes. Keeping them in a gitignored file is
> the habit that stops your database password ending up in a public repo.
> Commit `db.properties.example` (the template), never `db.properties`.

---

## 6. Compile and run the test

From inside the project folder:
```bash
# compile everything, with the JDBC driver on the classpath
javac -cp ".:postgresql-42.7.3.jar" *.java

# run the manual test (the '.' keeps db.properties findable)
java -cp ".:postgresql-42.7.3.jar" ProductDAOTest
```

> On **Windows**, the classpath separator is `;` not `:` —
> use `-cp ".;postgresql-42.7.3.jar"`.

### Expected output

```
=== All Products ===
Product{id=1, name='Wireless Earbuds', price=15000.00, stockQty=50}
Product{id=2, name='Cotton T-Shirt', price=4500.00, stockQty=100}
Product{id=3, name='Table Lamp', price=8000.00, stockQty=30}

=== Product by ID (1) ===
Product{id=1, name='Wireless Earbuds', price=15000.00, stockQty=50}

=== Adding a new product ===
Product added: true

=== All Products After Insert ===
... (now includes a 4th product, "Bluetooth Speaker")
```

Each run of the test inserts another "Bluetooth Speaker", so the product
count grows by one each time. To reset to a clean state, re-run `schema.sql`
(see step 4).

---

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `permission denied for schema public` | `shopease_admin` doesn't own the DB. See the fix note in step 3. |
| `Could not find db.properties` | You skipped step 5, or you're running from the wrong directory. Run from the project folder. |
| `password authentication failed` | Wrong password in `db.properties`, or the role's password differs from step 3. |
| `Connection refused` | PostgreSQL isn't running. Start it (`sudo service postgresql start`). |
| `ClassNotFoundException: org.postgresql.Driver` | The JDBC jar isn't on your classpath. Check the `-cp` value. |

---

## Project file map

| File | Purpose |
|---|---|
| `schema.sql` | Creates all tables + seed data |
| `db.properties.example` | Template for credentials (safe to commit) |
| `db.properties` | Your real credentials (gitignored) |
| `DBConnection.java` | Opens DB connections; loads `db.properties` |
| `Product.java` | Java object mirroring one product row |
| `ProductDAO.java` | Interface: the allowed product operations |
| `ProductDAOImpl.java` | JDBC implementation of those operations |
| `ProductDAOTest.java` | Manual test that exercises the DAO |

