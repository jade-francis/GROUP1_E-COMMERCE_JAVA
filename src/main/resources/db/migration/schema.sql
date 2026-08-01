-- ============================================
-- shopease E-Commerce Database Schema
-- ============================================
-- Run with: psql -U shopease_admin -d shopease -f schema.sql

-- Drop tables if re-running during development (safe to remove later)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================
-- USERS
-- ============================================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'customer', -- 'customer' or 'admin'
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================
-- CATEGORIES
-- ============================================
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- ============================================
-- PRODUCTS
-- ============================================
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    stock_qty INTEGER NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================
-- CART_ITEMS (a user's active/unpurchased cart)
-- ============================================
CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (user_id, product_id) -- prevents duplicate rows for same product in same cart
);

-- ============================================
-- ORDERS (a completed/placed order)
-- ============================================
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    total_amount NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending, paid, shipped, delivered, cancelled
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================
-- ORDER_ITEMS (junction table: many products per order)
-- price_at_purchase locks in the price at time of sale
-- so later price changes don't rewrite order history
-- ============================================
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_purchase NUMERIC(10,2) NOT NULL CHECK (price_at_purchase >= 0)
);

-- ============================================
-- Helpful indexes (speeds up common lookups)
-- ============================================
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_cart_user ON cart_items(user_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- ============================================
-- Sample seed data (optional — useful for early testing)
-- ============================================
INSERT INTO categories (name, description) VALUES
('Electronics', 'Phones, laptops, and accessories'),
('Fashion', 'Clothing and accessories'),
('Home & Living', 'Furniture and home goods');

INSERT INTO products (name, description, price, stock_qty, category_id) VALUES
('Wireless Earbuds', 'Bluetooth 5.0 earbuds with charging case', 15000.00, 50, 1),
('Cotton T-Shirt', 'Plain unisex cotton t-shirt', 4500.00, 100, 2),
('Table Lamp', 'LED desk lamp with adjustable brightness', 8000.00, 30, 3);
