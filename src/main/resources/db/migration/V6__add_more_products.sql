INSERT INTO products (name, description, price, stock_quantity, category_id, image_url)
SELECT product.name, product.description, product.price, product.stock_quantity, category.id, product.image_url
FROM (VALUES
    ('Smart Watch', 'Fitness tracking smart watch with heart-rate monitoring', 28500.00, 35, 'Electronics', '/images/smart-watch.jpg'),
    ('Portable Bluetooth Speaker', 'Compact wireless speaker with rich sound and long battery life', 19500.00, 42, 'Electronics', '/images/bluetooth-speaker.jpg'),
    ('Laptop Backpack', 'Water-resistant backpack with a padded laptop compartment', 12500.00, 60, 'Fashion', '/images/laptop-backpack.jpg'),
    ('Classic Sneakers', 'Comfortable everyday lace-up sneakers', 22000.00, 48, 'Fashion', '/images/classic-sneakers.jpg'),
    ('Denim Jacket', 'Versatile unisex denim jacket with a relaxed fit', 18000.00, 27, 'Fashion', '/images/denim-jacket.jpg'),
    ('Throw Pillow Set', 'Set of two soft decorative pillows for sofas and beds', 7500.00, 55, 'Home & Living', '/images/throw-pillows.jpg'),
    ('Electric Kettle', 'Fast-boiling 1.7-litre electric kettle with auto shut-off', 14500.00, 33, 'Home & Living', '/images/electric-kettle.jpg'),
    ('Storage Basket', 'Woven multipurpose basket for tidy home organization', 6500.00, 70, 'Home & Living', '/images/storage-basket.jpg'),
    ('Wireless Keyboard', 'Slim rechargeable keyboard for laptops, tablets and desktops', 16500.00, 40, 'Electronics', '/images/wireless-keyboard.jpg')
) AS product(name, description, price, stock_quantity, category_name, image_url)
JOIN categories category ON category.name = product.category_name
WHERE NOT EXISTS (SELECT 1 FROM products existing WHERE existing.name = product.name);
