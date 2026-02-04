-- =========================
-- SEED SAMPLE DATA FOR TESTING
-- =========================

-- Insert sample products
INSERT INTO products (product_id, name, description, price, available_qty) VALUES
(gen_random_uuid(), 'Laptop', 'High-performance laptop with 16GB RAM', 999.99, 50),
(gen_random_uuid(), 'Smartphone', 'Latest smartphone with 128GB storage', 699.99, 100),
(gen_random_uuid(), 'Headphones', 'Wireless noise-cancelling headphones', 199.99, 200),
(gen_random_uuid(), 'Keyboard', 'Mechanical gaming keyboard with RGB', 149.99, 150),
(gen_random_uuid(), 'Mouse', 'Ergonomic wireless mouse', 49.99, 300),
(gen_random_uuid(), 'Monitor', '27-inch 4K UHD monitor', 399.99, 75),
(gen_random_uuid(), 'Webcam', 'HD webcam with microphone', 79.99, 120),
(gen_random_uuid(), 'USB Cable', 'USB-C to USB-C cable 2m', 19.99, 500),
(gen_random_uuid(), 'Desk Lamp', 'LED desk lamp with adjustable brightness', 39.99, 180),
(gen_random_uuid(), 'Backpack', 'Laptop backpack with multiple compartments', 59.99, 250);