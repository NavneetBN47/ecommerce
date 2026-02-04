-- =========================
-- SEED DATA FOR SHOPPING CART SYSTEM
-- =========================

-- Insert sample products
INSERT INTO products (product_id, name, description, price, available_qty) VALUES
    (gen_random_uuid(), 'Laptop', 'High-performance laptop with 16GB RAM', 1299.99, 50),
    (gen_random_uuid(), 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 200),
    (gen_random_uuid(), 'Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 89.99, 100),
    (gen_random_uuid(), 'USB-C Hub', '7-in-1 USB-C hub with HDMI and card reader', 49.99, 150),
    (gen_random_uuid(), 'Monitor', '27-inch 4K UHD monitor', 399.99, 75),
    (gen_random_uuid(), 'Webcam', '1080p HD webcam with built-in microphone', 79.99, 120),
    (gen_random_uuid(), 'Headphones', 'Noise-cancelling wireless headphones', 199.99, 80),
    (gen_random_uuid(), 'External SSD', '1TB portable external SSD', 129.99, 90),
    (gen_random_uuid(), 'Desk Lamp', 'LED desk lamp with adjustable brightness', 39.99, 180),
    (gen_random_uuid(), 'Cable Organizer', 'Desktop cable management system', 19.99, 250);

-- Insert sample users (passwords are hashed with BCrypt for 'password123')
INSERT INTO users (user_id, username, password_hash, full_name, email) VALUES
    (gen_random_uuid(), 'john_doe', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J8fIrCJ6tWJHqz8Kp.xQYHqYVjKrCu', 'John Doe', 'john.doe@example.com'),
    (gen_random_uuid(), 'jane_smith', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J8fIrCJ6tWJHqz8Kp.xQYHqYVjKrCu', 'Jane Smith', 'jane.smith@example.com'),
    (gen_random_uuid(), 'bob_wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMye7J8fIrCJ6tWJHqz8Kp.xQYHqYVjKrCu', 'Bob Wilson', 'bob.wilson@example.com');

-- Note: No carts are created for seed users as per business rules
-- Carts will be created lazily when users add their first product