# ShopEase Server (Spring Boot)

Backend web layer for ShopEase. It **wraps the already-tested JDBC DAO layer**
(from the sibling `ShopEase-db` project) behind a Spring Boot REST API. The DAO
code is unchanged except for being moved into the `com.shopease.db` package.

## Prerequisites

- **JDK 17–21** (Spring Boot 3.3 targets these; `pom.xml` is set to 21).
  The dev machine currently has JDK 25 — if the build complains about the Java
  version, install JDK 21 (e.g. `sdk install java 21-tem` via SDKMAN) and point
  `JAVA_HOME` at it.
- **Maven** (`mvn`). If not installed: `sudo apt install maven`
  (or use the Maven Wrapper if we add one later).
- A running **PostgreSQL** with the `shopease` database + schema loaded
  (see `../ShopEase-db/SETUP.md` and `schema.sql`).

## Configuration

Database credentials are read from `src/main/resources/db.properties`
(gitignored). Copy the template and fill in real values:

```
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

This is the SAME mechanism the standalone DAO project uses — the DAO layer's
`DBConnection` loads it from the classpath. No Spring `spring.datasource.*`
config is needed with the current design.

## Run

```
mvn spring-boot:run
```

Then:

```
curl http://localhost:8080/api/products
curl http://localhost:8080/api/products/1
curl "http://localhost:8080/api/products?categoryId=1"
curl -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -d '{"name":"Bluetooth Speaker","description":"Portable","price":12000.00,"stockQty":20,"categoryId":1}'
```

## Architecture / how to extend

Three layers, one direction of dependency:

```
Controller (HTTP)  ->  Service (business rules)  ->  DAO (SQL)
com.shopease.web       com.shopease.web              com.shopease.db
```

- `com.shopease.db` — the tested DAO layer (Product, User, Category, Cart,
  Order, OrderItem). Do not edit; it has end-to-end tests in `ShopEase-db`.
- `config/DaoConfig` — registers each DAO impl as a Spring `@Bean` so they can
  be injected. (We chose `@Bean` over adding `@Repository` to the impls so the
  tested files stay byte-for-byte identical.)
- `web/ProductService` + `web/ProductController` — the **worked template**.
  To add another resource (cart, orders, ...), copy this pair:
  1. The DAO bean already exists in `DaoConfig`.
  2. Write `<Thing>Service` injecting the DAO.
  3. Write `<Thing>Controller` injecting the service.

The Order checkout (`OrderDAO.placeOrder`) already runs as a single JDBC
transaction internally, so a future `OrderController` can just call it — no
`@Transactional` needed at the Spring layer for that flow.
