CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER'
        CHECK (role IN ('CUSTOMER', 'ADMIN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0
        CHECK (stock_quantity >= 0),
    category_id BIGINT REFERENCES categories(id)
        ON DELETE SET NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id)
        ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id)
        ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1
        CHECK (quantity > 0),
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_cart_product UNIQUE (user_id, product_id)
);

CREATE TABLE orders (
      id BIGSERIAL PRIMARY KEY,
      user_id BIGINT NOT NULL REFERENCES users(id)
          ON DELETE RESTRICT,
      total_amount NUMERIC(12, 2) NOT NULL
          CHECK (total_amount >= 0),
      status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
          CHECK (status IN (
              'PENDING',
              'PAID',
              'SHIPPED',
              'DELIVERED',
              'CANCELLED'
          )),
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  );

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id)
        ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id)
        ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_purchase NUMERIC(12, 2) NOT NULL
        CHECK (price_at_purchase >= 0)
);

CREATE INDEX idx_products_category
    ON products(category_id);

CREATE INDEX idx_cart_items_user
    ON cart_items(user_id);

CREATE INDEX idx_orders_user
    ON orders(user_id);

CREATE INDEX idx_order_items_order
    ON order_items(order_id);

  -- Seed categories.
INSERT INTO categories (name, description)
VALUES
    ('Electronics', 'Phones, laptops, and accessories'),
    ('Fashion', 'Clothing and accessories'),
    ('Home & Living', 'Furniture and home goods')
ON CONFLICT (name) DO NOTHING;

  -- Seed products.
INSERT INTO products (
    name,
    description,
    price,
    stock_quantity,
    category_id,
    image_url
)
 SELECT
    'Wireless Earbuds',
    'Bluetooth 5.0 earbuds with charging case',
    15000.00,
    50,
    id,
    '/images/wireless-earbuds.jpg'
FROM categories
WHERE name = 'Electronics'
AND NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Wireless Earbuds'
);

INSERT INTO products (
    name,
    description,
    price,
    stock_quantity,
    category_id,
    image_url
)
SELECT
    'Cotton T-Shirt',
    'Plain unisex cotton t-shirt',
    4500.00,
    100,
    id,
    '/images/cotton-t-shirt.jpg'
FROM categories
WHERE name = 'Fashion'
AND NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Cotton T-Shirt'
);

INSERT INTO products (
    name,
    description,
    price,
    stock_quantity,
    category_id,
    image_url
)
SELECT
    'Table Lamp',
    'LED desk lamp with adjustable brightness',
    8000.00,
    30,
    id,
    '/images/table-lamp.jpg'
FROM categories
WHERE name = 'Home & Living'
AND NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Table Lamp'
);