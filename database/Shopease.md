# ShopEase Project Status

## What this is

ShopEase is an e-commerce site built as a Java coursework group project. I
(cypher) own the **database side** with one teammate; a separate teammate owns
the backend server. Tech stack: **plain JDBC + PostgreSQL** (no ORM). The
database layer is a set of DAO classes (interface + JDBC impl + model) plus a
`schema.sql`. Each DAO is considered done only once it has an end-to-end manual
test (`*Test.java` with a `main()`) that has been run against the real
`shopease` database and verified by eye.

## Done (tested and working)

- **schema.sql** — 6 tables: `users`, `categories`, `products`, `cart_items`,
  `orders`, `order_items`. Includes CHECK constraints, FKs, indexes, and sample
  seed data (3 categories, 3 products).
- **DBConnection** — reads credentials from `db.properties` and connects.
- **ProductDAO** (interface + `ProductDAOImpl` + `Product` model) — full CRUD.
  Tested via `ProductDAOTest`, working.
- **UserDAO** (interface + `UserDAOImpl` + `User` model) — register/login with
  **BCrypt** password hashing. Tested via `UserDAOTest` (verifies hash storage,
  correct/wrong password, duplicate-email rejection), working.
- **CategoryDAO** (interface + `CategoryDAOImpl` + `Category` model) — full CRUD:
  getAllCategories, getCategoryById, getCategoryByName, addCategory (returns the
  created Category, null on duplicate name), updateCategory, deleteCategory.
  Tested via **`CategoryDAOTest`**, run end-to-end and verified: add/read-back,
  duplicate-name rejection (UNIQUE constraint → null), update, delete, and seed
  data left untouched. **DONE.**
- **CartDAO** (interface + `CartDAOImpl` + `CartItem` model) — add (upsert),
  getCart (joined with product name/price), updateQuantity, removeFromCart,
  clearCart, getCartTotal. Tested via **`CartDAOTest`**, run end-to-end and
  verified: upsert bumps quantity instead of duplicating, `updateQuantity(...,0)`
  removes the line, totals match, cascade delete cleans up. **DONE.**
- **OrderDAO + OrderItemDAO** (interfaces + `OrderDAOImpl`/`OrderItemDAOImpl` +
  `Order`/`OrderItem` models) — the **transactional checkout**. `placeOrder`
  turns a cart into an order atomically: reads cart (locking product rows with
  `SELECT ... FOR UPDATE OF p`), verifies stock, inserts the `orders` row,
  inserts one `order_items` row per line (snapshotting `price_at_purchase`),
  decrements `products.stock_qty`, clears the cart — all on ONE connection with
  `setAutoCommit(false)`, commit at the end, `rollback()` on any failure. Also
  `getOrderById`, `getOrdersByUser`, `updateStatus`. Tested via **`OrderDAOTest`**,
  run end-to-end and verified BOTH paths: happy path (correct total, stock
  decremented, cart cleared, price snapshotted) AND rollback paths (empty cart →
  null; insufficient stock → null with stock/cart/orders all unchanged). **DONE.**
- **SETUP.md** — written for teammates to get the DB running.
- **ShopEase-server (Spring Boot backend)** — scaffolded at
  `~/Desktop/ShopEase-server/` (SEPARATE project from ShopEase-db). Maven,
  Spring Boot 3.3.5. Wraps the tested DAO layer instead of rewriting it:
  the DAO sources were COPIED into package `com.shopease.db` (only a `package`
  line added — no logic change; ShopEase-db is untouched). `config/DaoConfig`
  registers each DAO impl as a `@Bean` (chosen over adding `@Repository` to keep
  the tested files identical). One worked vertical exists as the template:
  `web/ProductService` + `web/ProductController` (GET all / by id /
  ?categoryId= , POST). DAO layer recompiles clean in its new package (19
  classes). **BOOTED AND VERIFIED (2026-08-01):** starts on Java 21.0.11,
  Tomcat :8080; `GET /api/products` returns the live DB rows as JSON (200),
  `/api/products/1` (200), `?categoryId=2` filters (200), `/api/products/999`
  → 404. Full Controller→Service→ProductDAO→Postgres chain works.

## In progress

- Nothing actively mid-write. ShopEase-server is up and the Product vertical is
  proven end-to-end against the live database. Ready for the teammate to extend.

## Next up

1. **Backend teammate copies the Product vertical** (Service + Controller) for
   the remaining resources: User (login/register), Category, Cart, Order
   (checkout — `OrderDAO.placeOrder` is already transactional, so the controller
   just calls it).
2. (Possible follow-ups if scope grows) order cancellation that restocks
   inventory; pagination on product/order listings; admin views.

## Known gotchas / decisions made

- **Credentials are externalized** to `db.properties`, which is **gitignored**.
  A `db.properties.example` template is committed so teammates know what keys to
  fill in. Reason: never commit real DB credentials to the shared repo.
- **BCrypt** (jbcrypt-0.4.jar) chosen for password hashing — salted + slow by
  design, so stored hashes resist brute-forcing; the backend never stores or
  compares plain-text passwords.
- **Postgres upsert** (`INSERT ... ON CONFLICT (user_id, product_id) DO UPDATE`)
  is used in `CartDAOImpl.addToCart`, relying on the `UNIQUE(user_id, product_id)`
  constraint in `cart_items`. Adding a product already in the cart bumps its
  quantity instead of erroring or duplicating the row.
- **`updateQuantity(userId, productId, <=0)`** intentionally delegates to
  `removeFromCart` — the `CHECK (quantity > 0)` constraint would reject 0
  anyway, so "set to 0" means "remove."
- **`order_items.price_at_purchase`** locks in the price at time of sale so later
  product price changes don't rewrite historical order totals. `placeOrder`
  snapshots the cart's current product price into this column; all reads of an
  order use this snapshot, never the live `products.price`.
- **Transaction pattern (OrderDAOImpl.placeOrder):** everything runs on ONE
  connection with `setAutoCommit(false)`, `commit()` at the end, `rollback()` in
  the catch, and `finally` restores autocommit + closes. Deliberately does NOT
  call CartDAO/ProductDAO inside the transaction (those open their own
  connections, outside the transaction) — instead it has private helpers that
  take the shared `conn`. This is the reference pattern for any future
  multi-table write.
- **`SELECT ... FOR UPDATE OF p`** in the cart read inside `placeOrder` locks the
  referenced *product* rows (not the cart rows) for the duration of the
  transaction, so two concurrent checkouts can't both pass the stock check and
  oversell. Stock is verified before any write; the `CHECK (stock_qty >= 0)`
  constraint is a backstop.
- **FK delete behavior is deliberate:** `orders.user_id` is `ON DELETE RESTRICT`
  (don't let a user with order history be deleted), while `cart_items.user_id`
  is `ON DELETE CASCADE` (a deleted user's cart is disposable). This is why
  `CartDAOTest` can register a throwaway user, exercise the cart, then delete the
  user to clean up.
- **Tests are self-cleaning** — `UserDAOTest`/`CartDAOTest`/`OrderDAOTest` use a
  unique timestamp-based email each run, and `CategoryDAOTest` a unique
  timestamp-based category name, so re-running doesn't collide with UNIQUE
  constraints or pile up rows. `OrderDAOTest` must delete the user's orders
  BEFORE deleting the user (because `orders.user_id` is ON DELETE RESTRICT);
  order_items cascade off the orders.
- **Spring Boot chosen over servlets** for the backend, and the DAO layer is
  WRAPPED, not rewritten: DAOs registered as `@Bean`s in `DaoConfig` rather than
  annotated with `@Repository`, so the tested files stay byte-for-byte identical
  (apart from the package line). The server is a separate project
  (`ShopEase-server`) so `ShopEase-db` and its tests remain intact. Default
  package was the blocker — packaged Spring code can't import default-package
  classes, so the DAOs had to move into `com.shopease.db`. Spring uses the SAME
  `db.properties`/`DBConnection` mechanism (loaded from the classpath) rather
  than `spring.datasource.*`, to avoid touching DAO code.
- **ShopEase-server MUST build/run on JDK 21, not the system default JDK 25.**
  Spring Boot 3.3 targets Java 17–21; the machine's default is JDK 25. JDK 21 is
  installed alongside at `/usr/lib/jvm/java-21-openjdk-amd64` (system default
  left as 25 — `update-alternatives` untouched). To run the server, export
  `JAVA_HOME` for that one command so Maven forks the app on 21:
  `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run`
  (run from `~/Desktop/ShopEase-server`). Without this, Maven uses JDK 25 and
  Boot may fail. The pom also pins `<java.version>21</java.version>`.
- Project lives at `Desktop/ShopEase-db/`. Compile/run with the two jars on the
  classpath: `-cp ".:postgresql-42.7.3.jar:jbcrypt-0.4.jar"`.

---
_Last updated: 2026-08-01 — ShopEase-server booted and verified end-to-end on JDK 21 (installed alongside JDK 25); GET /api/products returns live DB data as JSON. Product vertical proven; ready for teammate to extend._
