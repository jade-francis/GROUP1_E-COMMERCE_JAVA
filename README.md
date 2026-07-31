 # Group 1 E-Commerce Application

  An e-commerce website built with Java, Spring Boot, Thymeleaf and PostgreSQL.

  ## Technologies

  - Java
  - Spring Boot
  - Spring MVC
  - Thymeleaf
  - Spring Data JPA
  - PostgreSQL
  - Maven

  ## Requirements

  - Java 17 or later
  - PostgreSQL
  - Git

  ## Local Setup

  1. Clone the repository.
  2. Create a PostgreSQL database named `ecommerce_db`.
  3. Copy `.env.example` to `.env`.

  Windows:

  ```cmd
  copy .env.example .env

  4. Edit .env with your PostgreSQL credentials.
  5. Start the application.

  Windows:

  mvnw.cmd spring-boot:run

  Linux/macOS:

  ./mvnw spring-boot:run

  Open:

  http://localhost:8080

  ## Environment Variables

   Variable       Description
  ━━━━━━━━━━━━━  ━━━━━━━━━━━━━━━━━━━━━
   DB_URL         PostgreSQL JDBC URL
  ─────────────  ─────────────────────
   DB_USERNAME    PostgreSQL username
  ─────────────  ─────────────────────
   DB_PASSWORD    PostgreSQL password

  Never commit .env.

  ## Initial Features

  - Product catalogue
  - User registration and login
  - Shopping cart
  - Checkout
  - Order history
  - Admin product management