-- Connect the locally supplied images to their matching products.
UPDATE products
SET image_url = '/images/smart_watch.jpg'
WHERE name = 'Smart Watch';

UPDATE products
SET image_url = '/images/portable_bluetooth_speaker.jpg'
WHERE name = 'Portable Bluetooth Speaker';

UPDATE products
SET image_url = '/images/denim_jacket.jpg'
WHERE name = 'Denim Jacket';

-- The remaining supplied product photos represent new fashion products.
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url)
SELECT product.name, product.description, product.price, product.stock_quantity, category.id, product.image_url
FROM (VALUES
    ('Embroidered Baseball Cap', 'Adjustable olive baseball cap with an embroidered lion design', 6500.00, 45, '/images/6017283056563064357.jpg'),
    ('Colourful Handbag Collection', 'Structured statement handbag available in vibrant colours', 24500.00, 24, '/images/6017283056563064358.jpg'),
    ('Classic Brown Handbag', 'Elegant brown top-handle handbag with a detachable shoulder strap', 18500.00, 30, '/images/6017283056563064363.jpg')
) AS product(name, description, price, stock_quantity, image_url)
JOIN categories category ON category.name = 'Fashion'
WHERE NOT EXISTS (
    SELECT 1 FROM products existing WHERE existing.name = product.name
);
