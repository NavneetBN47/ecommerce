-- =========================
-- MIGRATION V004: Seed Data
-- Created: 2026-03-12
-- Description: Inserts initial seed data for users and products
-- =========================

-- Seed Users (no carts created)
INSERT INTO users (username, password, full_name, email) VALUES
('mickey', 'password123', 'Mickey Mouse', 'mickey@cartoon.com'),
('minnie', 'password123', 'Minnie Mouse', 'minnie@cartoon.com'),
('donald', 'password123', 'Donald Duck', 'donald@cartoon.com'),
('goofy',  'password123', 'Goofy', 'goofy@cartoon.com'),
('pluto',  'password123', 'Pluto', 'pluto@cartoon.com');

-- Seed Products
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