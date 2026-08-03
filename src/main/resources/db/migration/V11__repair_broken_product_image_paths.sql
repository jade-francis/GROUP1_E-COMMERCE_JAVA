-- Repair legacy and seller-entered image values that do not resolve to image files.
UPDATE products
SET image_url = '/images/6017283056563064407.jpg'
WHERE name = 'Wireless Earbuds';

UPDATE products
SET image_url = '/images/6017283056563064412.jpg'
WHERE LOWER(name) = 'eva'
  AND (image_url IS NULL OR image_url NOT LIKE '/uploads/%');

UPDATE products
SET image_url = '/images/6017283056563064415.jpg'
WHERE LOWER(name) = 'raid'
  AND (image_url IS NULL OR image_url NOT LIKE '/uploads/%');
