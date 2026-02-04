-- Seed users
INSERT INTO users (username, password, full_name, email) VALUES
('mickey', 'password123', 'Mickey Mouse', 'mickey@cartoon.com'),
('minnie', 'password123', 'Minnie Mouse', 'minnie@cartoon.com'),
('donald', 'password123', 'Donald Duck', 'donald@cartoon.com'),
('goofy',  'password123', 'Goofy', 'goofy@cartoon.com'),
('pluto',  'password123', 'Pluto', 'pluto@cartoon.com')
ON CONFLICT (username) DO NOTHING;

-- Seed products
INSERT INTO products (product_name, description, price, available_qty, is_active) VALUES
('Laptop', 'High performance laptop', 1200.00, 10, true),
('Mouse', 'Wireless mouse', 25.00, 50, true),
('Keyboard', 'Mechanical keyboard', 80.00, 30, true),
('Monitor', '24 inch LED monitor', 180.00, 20, true),
('Headphones', 'Noise cancelling headphones', 150.00, 15, true),
('Webcam', 'HD webcam', 70.00, 25, true),
('Printer', 'All-in-one printer', 200.00, 10, true),
('Desk Lamp', 'LED desk lamp', 35.00, 40, true),
('USB Hub', '4-port USB hub', 20.00, 60, true),
('External HDD', '1TB external hard drive', 90.00, 18, true),
('SSD', '512GB solid state drive', 110.00, 22, true),
('Router', 'Wireless router', 95.00, 16, true),
('Smartphone', 'Android smartphone', 650.00, 12, true),
('Tablet', '10 inch tablet', 400.00, 14, true),
('Power Bank', '10000mAh power bank', 30.00, 45, true),
('Bluetooth Speaker', 'Portable speaker', 55.00, 28, true),
('Smart Watch', 'Fitness smart watch', 220.00, 10, true),
('Camera', 'Digital camera', 500.00, 8, true),
('Microphone', 'USB microphone', 85.00, 20, true),
('Gaming Chair', 'Ergonomic gaming chair', 300.00, 6, true)
ON CONFLICT DO NOTHING;
