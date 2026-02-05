-- Seed Data for E-Commerce Application

-- Insert Sample Users
INSERT INTO users (username, email, password, first_name, last_name, phone_number, role, status) VALUES
('admin', 'admin@ecommerce.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JDMmP8nKt4TY9qcrmD6vR6U3fl4E8e', 'Admin', 'User', '1234567890', 'ADMIN', 'ACTIVE'),
('john_doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JDMmP8nKt4TY9qcrmD6vR6U3fl4E8e', 'John', 'Doe', '9876543210', 'CUSTOMER', 'ACTIVE'),
('jane_smith', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/JDMmP8nKt4TY9qcrmD6vR6U3fl4E8e', 'Jane', 'Smith', '5551234567', 'CUSTOMER', 'ACTIVE');

-- Insert Sample Products
INSERT INTO products (name, description, sku, price, discount_price, stock_quantity, category, brand, active, featured) VALUES
('Laptop Pro 15', 'High-performance laptop with 16GB RAM and 512GB SSD', 'LAPTOP-001', 1299.99, 1199.99, 50, 'Electronics', 'TechBrand', TRUE, TRUE),
('Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 'MOUSE-001', 29.99, NULL, 200, 'Electronics', 'TechBrand', TRUE, FALSE),
('USB-C Cable', 'High-speed USB-C charging cable 6ft', 'CABLE-001', 12.99, 9.99, 500, 'Accessories', 'CableCo', TRUE, FALSE),
('Smartphone X', 'Latest smartphone with 128GB storage', 'PHONE-001', 899.99, 799.99, 100, 'Electronics', 'PhoneCorp', TRUE, TRUE),
('Bluetooth Headphones', 'Noise-cancelling wireless headphones', 'HEADPHONE-001', 199.99, 179.99, 75, 'Electronics', 'AudioTech', TRUE, TRUE),
('Laptop Bag', 'Durable laptop bag with multiple compartments', 'BAG-001', 49.99, NULL, 150, 'Accessories', 'BagMaster', TRUE, FALSE),
('Keyboard Mechanical', 'RGB mechanical gaming keyboard', 'KEYBOARD-001', 129.99, 99.99, 80, 'Electronics', 'GameGear', TRUE, FALSE),
('Monitor 27"', '27-inch 4K UHD monitor', 'MONITOR-001', 399.99, 349.99, 40, 'Electronics', 'DisplayPro', TRUE, TRUE),
('Webcam HD', '1080p HD webcam with microphone', 'WEBCAM-001', 79.99, 69.99, 120, 'Electronics', 'CamTech', TRUE, FALSE),
('External SSD 1TB', 'Portable external SSD 1TB', 'SSD-001', 149.99, 129.99, 90, 'Storage', 'StoragePlus', TRUE, FALSE);

-- Note: Password for all users is 'password123' (BCrypt encoded)