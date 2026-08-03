UPDATE products SET image_url = '/images/6017283056563064408.jpg' WHERE name = 'Wireless Keyboard';
UPDATE products SET image_url = '/images/6017283056563064413.jpg' WHERE name = 'Throw Pillow Set';

INSERT INTO products (name, description, price, stock_quantity, category_id, image_url)
SELECT product.name, product.description, product.price, product.stock_quantity, category.id, product.image_url
FROM (VALUES
    ('Wireless Earbuds', 'Compact true wireless earbuds with a portable charging case', 15500.00, 50, 'Electronics', '/images/6017283056563064407.jpg'),
    ('Gaming Keyboard and Mouse', 'RGB gaming keyboard and mouse combo', 23000.00, 30, 'Electronics', '/images/6017283056563064410.jpg'),
    ('Premium Wireless Earbuds', 'Noise-isolating wireless earbuds with charging case', 32000.00, 25, 'Electronics', '/images/6017283056563064414.jpg'),
    ('Fresh Beauty Soap', 'Gentle cleansing beauty soap for everyday use', 1800.00, 80, 'Fashion', '/images/6017283056563064412.jpg'),
    ('Multi-Purpose Insect Killer', 'Fast-acting household insect control spray', 4500.00, 45, 'Home & Living', '/images/6017283056563064415.jpg')
) AS product(name, description, price, stock_quantity, category_name, image_url)
JOIN categories category ON category.name = product.category_name
WHERE NOT EXISTS (SELECT 1 FROM products existing WHERE existing.name = product.name);
