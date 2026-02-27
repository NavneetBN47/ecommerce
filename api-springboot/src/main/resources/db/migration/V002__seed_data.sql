-- =========================
-- SEED DATA MIGRATION
-- Version: V002
-- Description: Insert initial seed data for users and products
-- =========================

-- SEED USERS
-- Note: Passwords are BCrypt hashed version of 'password123'
INSERT INTO users (username, password, full_name, email) VALUES
('mickey', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mickey Mouse', 'mickey@cartoon.com'),
('minnie', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Minnie Mouse', 'minnie@cartoon.com'),
('donald', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Donald Duck', 'donald@cartoon.com'),
('goofy',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Goofy', 'goofy@cartoon.com'),
('pluto',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Pluto', 'pluto@cartoon.com');

-- SEED PRODUCTS
INSERT INTO products (product_name, description, price, available_qty) VALUES
('Laptop', 'High performance laptop', 1200.00, 10),
('Mouse', 'Wireless mouse', 25.00, 50),
('Keyboard', 'Mechanical keyboard', 80.00, 30),
('Monitor', '24 inch LED monitor', 180.00, 20),
('Headphones', 'Noise cancelling headphones', 150.00, 15),
('Webcam', 'HD webcam', 70.00, 25),
('Printer', 'All-in-one printer', 200.00, 10),
('Desk Lamp', 'LED desk lamp', 35.00, 40),
('USB Hub', '4-port USB hub', 20.00, 60),
('External HDD', '1TB external hard drive', 90.00, 18),
('SSD', '512GB solid state drive', 110.00, 22),
('Router', 'Wireless router', 95.00, 16),
('Smartphone', 'Android smartphone', 650.00, 12),
('Tablet', '10 inch tablet', 400.00, 14),
('Power Bank', '10000mAh power bank', 30.00, 45),
('Bluetooth Speaker', 'Portable speaker', 55.00, 28),
('Smart Watch', 'Fitness smart watch', 220.00, 10),
('Camera', 'Digital camera', 500.00, 8),
('Microphone', 'USB microphone', 85.00, 20),
('Gaming Chair', 'Ergonomic gaming chair', 300.00, 6);