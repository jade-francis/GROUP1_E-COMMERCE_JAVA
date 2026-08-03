ALTER TABLE orders ADD COLUMN payment_method VARCHAR(30) NOT NULL DEFAULT 'PAY_ON_DELIVERY';
ALTER TABLE orders ADD CONSTRAINT orders_payment_method_check CHECK (payment_method IN ('PAY_ON_DELIVERY'));
