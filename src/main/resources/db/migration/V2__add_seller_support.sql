ALTER TABLE users ADD COLUMN seller_status VARCHAR(20) NOT NULL DEFAULT 'NOT_SELLER'
    CHECK (seller_status IN ('NOT_SELLER', 'PENDING', 'APPROVED', 'SUSPENDED'));

ALTER TABLE products ADD COLUMN seller_id BIGINT REFERENCES users(id) ON DELETE RESTRICT;
CREATE INDEX idx_products_seller ON products(seller_id);
