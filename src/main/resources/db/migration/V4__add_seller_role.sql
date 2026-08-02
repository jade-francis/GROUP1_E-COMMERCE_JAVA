-- Add SELLER to role check constraint
ALTER TABLE users DROP CONSTRAINT users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (role IN ('CUSTOMER', 'SELLER', 'ADMIN'));

-- Also add seller_status column if not already there (from V2)
-- Note: V2 already added seller_status, but this migration ensures the role constraint includes SELLER