# ShopEase

ShopEase is a server-rendered e-commerce application built with Java, Spring Boot,
Thymeleaf, Spring JDBC, and PostgreSQL. It supports customer shopping, seller
approval and product management, and administrative oversight.

API details and integration examples are available in
[API_DOCUMENTATION.md](API_DOCUMENTATION.md). Image attribution is recorded in
[IMAGE_SOURCES.md](IMAGE_SOURCES.md).

## Features

### Customers

- Registration, email-code verified login, Google sign-in, logout, profiles, and password reset
- Randomized product discovery on the homepage and catalogue
- Product category filtering and product detail pages
- Shopping cart with quantity and stock validation
- Checkout with shipping address and pay-on-delivery
- Order history and order details
- Seller application workflow

### Sellers

- Admin-reviewed seller approval
- Seller dashboard
- Create, edit, and remove owned products
- Product images from direct URLs or device uploads
- Product image preview and broken-image fallback
- View seller orders and update fulfillment status

### Administrators

- Dashboard totals for products, orders, users, and seller requests
- Create, edit, and remove products
- Approve, suspend, or revoke seller access
- View and remove non-admin users
- Admin account initialization from environment variables

## Technology

- Java 21
- Spring Boot 3.5
- Spring MVC and Thymeleaf
- Spring Security
- Spring JDBC
- PostgreSQL
- Flyway database migrations
- Maven Wrapper

## Requirements

- Java 21 or later
- PostgreSQL
- Git

No global Maven installation is required because the repository includes Maven
Wrapper scripts.

## Local Setup

1. Clone the repository and enter the project directory.
2. Create a PostgreSQL database named `shopease_db`.
3. Create the local environment file.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux or macOS:

```bash
cp .env.example .env
```

4. Update `.env` with the PostgreSQL credentials, initial admin account, SMTP
   credentials, and Google OAuth credentials.

```properties
DB_URL=jdbc:postgresql://localhost:5432/shopease_db
DB_USERNAME=postgres
DB_PASSWORD=your_database_password
SHOPEASE_ADMIN_EMAIL=admin@example.com
SHOPEASE_ADMIN_PASSWORD=use_a_strong_password
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_email_app_password
MAIL_FROM=your_email@gmail.com
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

Password sign-in sends a six-digit code through SMTP. For Gmail, use an app
password rather than your normal account password.

Create a Google Cloud OAuth 2.0 Web application client and add this authorized
redirect URI for local development:

```text
http://localhost:8080/login/oauth2/code/google
```

Add the equivalent HTTPS URI for production, for example
`https://your-domain.com/login/oauth2/code/google`.

5. Start the application.

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux or macOS:

```bash
./mvnw spring-boot:run
```

6. Open `http://localhost:8080`.

Flyway creates and updates the schema automatically when the application starts.

## Environment Variables

| Variable | Required | Description |
| --- | --- | --- |
| `DB_URL` | Yes | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | Yes | PostgreSQL username |
| `DB_PASSWORD` | Yes | PostgreSQL password |
| `SHOPEASE_ADMIN_EMAIL` | Yes | Email used to create or update the admin account |
| `SHOPEASE_ADMIN_PASSWORD` | Yes | Admin password; use a strong production secret |
| `SHOPEASE_UPLOAD_DIR` | No | Product upload directory; defaults to `uploads/products` |
| `SPACES_ENDPOINT` | Production uploads | Spaces endpoint, for example `https://nyc3.digitaloceanspaces.com` |
| `SPACES_REGION` | Production uploads | Spaces region, for example `nyc3` |
| `SPACES_BUCKET` | Production uploads | Spaces bucket name |
| `SPACES_ACCESS_KEY` | Production uploads | Spaces access key stored as a secret |
| `SPACES_SECRET_KEY` | Production uploads | Spaces secret key stored as a secret |
| `SPACES_PUBLIC_URL` | Production uploads | Public bucket or CDN base URL |

Never commit `.env`. It is excluded by `.gitignore`.

## Admin Account

At startup, ShopEase creates the configured admin account if it does not exist.
If the configured email already exists, that account is updated to the admin role.
Changing the environment variable does not automatically delete an older admin
account; remove obsolete accounts explicitly from the admin user screen or database.

## Product Images

Bundled catalogue images are stored in
`src/main/resources/static/images`. Images uploaded by sellers or administrators
are written to `SHOPEASE_UPLOAD_DIR` and served under `/uploads/products/`.

Uploads support JPG, PNG, WebP, and GIF files up to 8 MB. When both a direct image
URL and a file are supplied, the uploaded file takes priority.

For production deployments, configure `SHOPEASE_UPLOAD_DIR` on persistent storage.
Files stored on an ephemeral application filesystem will be lost after a restart
or redeployment.

When all `SPACES_*` variables are configured, uploads are sent to DigitalOcean
Spaces instead of local disk. The Space must permit public reads for product images.

## Build And Test

Run the test suite:

```powershell
.\mvnw.cmd test
```

Linux or macOS:

```bash
./mvnw test
```

Create the executable JAR:

```powershell
.\mvnw.cmd clean package
```

The generated artifact is written to `target/shopease-0.0.1-SNAPSHOT.jar`.

Run the packaged application:

```powershell
java -jar target/shopease-0.0.1-SNAPSHOT.jar
```

## Production Checklist

- Use a managed PostgreSQL database and production credentials.
- Set a strong admin password through the deployment platform's secret manager.
- Mount persistent storage and set `SHOPEASE_UPLOAD_DIR` to that path.
- Confirm all Flyway migrations run successfully during startup.
- Use HTTPS through the hosting platform or reverse proxy.
- Do not deploy the local `.env` file.

## DigitalOcean App Platform

The repository includes a production `Dockerfile` and an App Platform template at
`.do/app.yaml`.

1. Create a DigitalOcean Managed PostgreSQL database.
2. Create a DigitalOcean Space and generate an access key.
3. Edit `.do/app.yaml` and replace `YOUR_GITHUB_USERNAME/YOUR_REPOSITORY`.
4. In DigitalOcean, create an app from the repository or import the app spec.
5. Add the database and admin variables as encrypted runtime secrets.
6. Add the six `SPACES_*` variables. Set `SPACES_PUBLIC_URL` to the Space CDN URL
   if CDN is enabled, or the public Space URL otherwise.
7. Deploy and confirm Flyway reports all migrations successfully applied.

The application listens on the `PORT` supplied by App Platform and trusts forwarded
HTTPS headers. Product uploads automatically use Spaces when its configuration is
complete and fall back to local storage during development.

## Project Structure

```text
src/main/java/com/group1/shopease/
  config/       Security, MVC, database, and admin initialization
  controller/   HTML page and REST API controllers
  model/        Application models and records
  repository/   Spring JDBC database access
  service/      Business logic and image storage

src/main/resources/
  db/migration/ Flyway SQL migrations
  static/       CSS, JavaScript, and bundled images
  templates/    Thymeleaf views
```
