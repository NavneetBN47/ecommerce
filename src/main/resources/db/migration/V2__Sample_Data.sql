-- Insert sample users
INSERT INTO users (email, password_hash, first_name, last_name, phone) VALUES
('john.doe@example.com', '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUa35gJdCr2v2QbVFzu', 'John', 'Doe', '1234567890'),
('jane.smith@example.com', '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUa35gJdCr2v2QbVFzu', 'Jane', 'Smith', '0987654321');

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, category, brand, image_url, active) VALUES
('Laptop Pro 15', 'High-performance laptop with 16GB RAM and 512GB SSD', 1299.99, 50, 'Electronics', 'TechBrand', 'https://example.com/laptop.jpg', TRUE),
('Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 29.99, 200, 'Electronics', 'TechBrand', 'https://example.com/mouse.jpg', TRUE),
('USB-C Cable', 'Durable USB-C charging cable 6ft', 12.99, 500, 'Accessories', 'CableCo', 'https://example.com/cable.jpg', TRUE),
('Bluetooth Headphones', 'Noise-cancelling over-ear headphones', 199.99, 100, 'Electronics', 'AudioTech', 'https://example.com/headphones.jpg', TRUE),
('Phone Case', 'Protective case for smartphones', 19.99, 300, 'Accessories', 'CaseMaster', 'https://example.com/case.jpg', TRUE),
('Portable Charger', '20000mAh power bank with fast charging', 49.99, 150, 'Electronics', 'PowerPlus', 'https://example.com/charger.jpg', TRUE),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 39.99, 80, 'Home', 'LightCo', 'https://example.com/lamp.jpg', TRUE),
('Notebook Set', 'Pack of 3 premium notebooks', 15.99, 250, 'Stationery', 'PaperPro', 'https://example.com/notebook.jpg', TRUE),
('Water Bottle', 'Insulated stainless steel water bottle 32oz', 24.99, 180, 'Home', 'HydroFlask', 'https://example.com/bottle.jpg', TRUE),
('Backpack', 'Laptop backpack with multiple compartments', 59.99, 120, 'Accessories', 'BagMaster', 'https://example.com/backpack.jpg', TRUE);