UPDATE products SET image_url = '/images/cotton-t-shirt.jpg' WHERE name = 'Cotton T-Shirt';
UPDATE products SET image_url = '/images/table-lamp.jpg' WHERE name = 'Table Lamp';
UPDATE products SET image_url = '/images/laptop-backpack.jpg' WHERE name = 'Laptop Backpack';
UPDATE products SET image_url = '/images/classic-sneakers.jpg' WHERE name = 'Classic Sneakers';

INSERT INTO products (name, description, price, stock_quantity, category_id, image_url)
SELECT product.name, product.description, product.price, product.stock_quantity, category.id, product.image_url
FROM (VALUES
    ('Wireless Over-Ear Headphones', 'Comfortable wireless headphones with immersive sound', 27500.00, 32, 'Electronics', '/images/wireless-headphones.jpg'),
    ('Classic Sunglasses', 'Timeless sunglasses with UV-protective lenses', 9500.00, 44, 'Fashion', '/images/classic-sunglasses.jpg'),
    ('Minimal Wristwatch', 'Clean modern wristwatch for everyday wear', 21000.00, 28, 'Fashion', '/images/minimal-wristwatch.jpg'),
    ('Luxury Perfume', 'Elegant long-lasting fragrance in a premium bottle', 26000.00, 22, 'Fashion', '/images/luxury-perfume.jpg'),
    ('Digital Camera', 'Compact digital camera for sharp everyday photography', 185000.00, 14, 'Electronics', '/images/digital-camera.jpg')
) AS product(name, description, price, stock_quantity, category_name, image_url)
JOIN categories category ON category.name = product.category_name
WHERE NOT EXISTS (SELECT 1 FROM products existing WHERE existing.name = product.name);
