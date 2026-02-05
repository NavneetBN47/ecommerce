-- Seed Data for E-Commerce Application

-- Insert Sample Users
INSERT INTO users (username, email, password, first_name, last_name, phone_number, active) VALUES
('john_doe', 'john.doe@example.com', 'password123', 'John', 'Doe', '+1234567890', TRUE),
('jane_smith', 'jane.smith@example.com', 'password123', 'Jane', 'Smith', '+1234567891', TRUE),
('admin_user', 'admin@example.com', 'admin123', 'Admin', 'User', '+1234567892', TRUE);

-- Insert Sample Products
INSERT INTO products (sku, name, description, price, stock, category, image_url, active) VALUES
('LAPTOP-001', 'Dell XPS 15', 'High-performance laptop with 16GB RAM and 512GB SSD', 1299.99, 50, 'Electronics', 'https://example.com/laptop1.jpg', TRUE),
('LAPTOP-002', 'MacBook Pro 14', 'Apple MacBook Pro with M2 chip', 1999.99, 30, 'Electronics', 'https://example.com/laptop2.jpg', TRUE),
('PHONE-001', 'iPhone 14 Pro', 'Latest iPhone with advanced camera system', 999.99, 100, 'Electronics', 'https://example.com/phone1.jpg', TRUE),
('PHONE-002', 'Samsung Galaxy S23', 'Flagship Android phone with stunning display', 899.99, 80, 'Electronics', 'https://example.com/phone2.jpg', TRUE),
('HEADPHONE-001', 'Sony WH-1000XM5', 'Premium noise-cancelling headphones', 349.99, 150, 'Audio', 'https://example.com/headphone1.jpg', TRUE),
('HEADPHONE-002', 'AirPods Pro', 'Apple wireless earbuds with active noise cancellation', 249.99, 200, 'Audio', 'https://example.com/headphone2.jpg', TRUE),
('TABLET-001', 'iPad Air', 'Versatile tablet with M1 chip', 599.99, 75, 'Electronics', 'https://example.com/tablet1.jpg', TRUE),
('WATCH-001', 'Apple Watch Series 8', 'Advanced health and fitness tracking', 399.99, 120, 'Wearables', 'https://example.com/watch1.jpg', TRUE),
('KEYBOARD-001', 'Mechanical Gaming Keyboard', 'RGB backlit mechanical keyboard', 129.99, 90, 'Accessories', 'https://example.com/keyboard1.jpg', TRUE),
('MOUSE-001', 'Wireless Gaming Mouse', 'High-precision wireless mouse', 79.99, 110, 'Accessories', 'https://example.com/mouse1.jpg', TRUE);