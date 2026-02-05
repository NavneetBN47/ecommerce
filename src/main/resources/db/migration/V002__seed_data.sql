-- V002__seed_data.sql
-- Seed data for testing and development

-- Insert sample users (passwords are BCrypt hashed 'password123')
INSERT INTO users (username, email, password, first_name, last_name, phone_number, active) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '+1234567890', true),
('jane_smith', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', '+1234567891', true),
('bob_wilson', 'bob.wilson@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Wilson', '+1234567892', true);

-- Insert sample products
INSERT INTO products (name, description, sku, price, stock_quantity, category, image_url, active) VALUES
('Laptop Pro 15', 'High-performance laptop with 15-inch display', 'LAP-001', 1299.99, 50, 'Electronics', 'https://example.com/images/laptop.jpg', true),
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'MOU-001', 29.99, 200, 'Electronics', 'https://example.com/images/mouse.jpg', true),
('Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 'KEY-001', 89.99, 100, 'Electronics', 'https://example.com/images/keyboard.jpg', true),
('USB-C Hub', '7-in-1 USB-C hub with HDMI and card reader', 'HUB-001', 49.99, 150, 'Electronics', 'https://example.com/images/hub.jpg', true),
('Laptop Backpack', 'Water-resistant laptop backpack with multiple compartments', 'BAG-001', 59.99, 75, 'Accessories', 'https://example.com/images/backpack.jpg', true),
('Webcam HD', '1080p HD webcam with built-in microphone', 'CAM-001', 79.99, 80, 'Electronics', 'https://example.com/images/webcam.jpg', true),
('Desk Lamp LED', 'Adjustable LED desk lamp with USB charging port', 'LAM-001', 39.99, 120, 'Accessories', 'https://example.com/images/lamp.jpg', true),
('Phone Stand', 'Adjustable phone stand for desk', 'STD-001', 19.99, 200, 'Accessories', 'https://example.com/images/stand.jpg', true),
('External SSD 1TB', 'Portable external SSD with 1TB capacity', 'SSD-001', 149.99, 60, 'Storage', 'https://example.com/images/ssd.jpg', true),
('Monitor 27 inch', '27-inch 4K UHD monitor with HDR support', 'MON-001', 399.99, 40, 'Electronics', 'https://example.com/images/monitor.jpg', true);

-- Note: Carts and orders are created dynamically by the application
-- No seed data needed for carts, cart_items, orders, and order_items