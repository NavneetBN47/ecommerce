-- V002__insert_seed_data.sql
-- Seed data for testing and development

-- Insert sample users (password is 'password123' encrypted with BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, phone_number, is_active) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '+1234567890', TRUE),
('jane_smith', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', '+1234567891', TRUE),
('bob_wilson', 'bob.wilson@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Wilson', '+1234567892', TRUE);

-- Insert sample products
INSERT INTO products (name, description, sku, price, stock_quantity, category, image_url, is_active) VALUES
('Laptop Pro 15', 'High-performance laptop with 15-inch display', 'LAPTOP-001', 1299.99, 50, 'Electronics', 'https://example.com/laptop.jpg', TRUE),
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'MOUSE-001', 29.99, 200, 'Electronics', 'https://example.com/mouse.jpg', TRUE),
('USB-C Cable', 'Premium USB-C charging cable 2m', 'CABLE-001', 19.99, 500, 'Accessories', 'https://example.com/cable.jpg', TRUE),
('Mechanical Keyboard', 'RGB mechanical gaming keyboard', 'KEYBOARD-001', 149.99, 75, 'Electronics', 'https://example.com/keyboard.jpg', TRUE),
('Laptop Bag', 'Durable laptop bag with multiple compartments', 'BAG-001', 49.99, 100, 'Accessories', 'https://example.com/bag.jpg', TRUE),
('Webcam HD', 'Full HD 1080p webcam with microphone', 'WEBCAM-001', 79.99, 150, 'Electronics', 'https://example.com/webcam.jpg', TRUE),
('Monitor 27"', '27-inch 4K UHD monitor', 'MONITOR-001', 399.99, 30, 'Electronics', 'https://example.com/monitor.jpg', TRUE),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 'LAMP-001', 39.99, 120, 'Accessories', 'https://example.com/lamp.jpg', TRUE),
('External SSD 1TB', 'Portable external SSD 1TB', 'SSD-001', 129.99, 80, 'Electronics', 'https://example.com/ssd.jpg', TRUE),
('Headphones Wireless', 'Noise-cancelling wireless headphones', 'HEADPHONE-001', 249.99, 60, 'Electronics', 'https://example.com/headphones.jpg', TRUE);