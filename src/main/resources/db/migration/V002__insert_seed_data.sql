-- V002__insert_seed_data.sql
-- Insert seed data for testing and development

-- Insert sample categories
INSERT INTO categories (name, description, parent_category_id, status) VALUES
('Electronics', 'Electronic devices and accessories', NULL, 'ACTIVE'),
('Clothing', 'Apparel and fashion items', NULL, 'ACTIVE'),
('Books', 'Books and publications', NULL, 'ACTIVE'),
('Home & Garden', 'Home improvement and garden supplies', NULL, 'ACTIVE'),
('Sports & Outdoors', 'Sports equipment and outdoor gear', NULL, 'ACTIVE');

-- Insert sample products
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, brand, image_url, status) VALUES
('ELEC-001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 100, 1, 'TechBrand', 'https://example.com/mouse.jpg', 'ACTIVE'),
('ELEC-002', 'Bluetooth Keyboard', 'Slim bluetooth keyboard for all devices', 49.99, 50, 1, 'TechBrand', 'https://example.com/keyboard.jpg', 'ACTIVE'),
('ELEC-003', 'USB-C Hub', '7-in-1 USB-C hub with HDMI and card reader', 39.99, 75, 1, 'TechBrand', 'https://example.com/hub.jpg', 'ACTIVE'),
('ELEC-004', 'Wireless Headphones', 'Noise-cancelling wireless headphones', 149.99, 30, 1, 'AudioPro', 'https://example.com/headphones.jpg', 'ACTIVE'),
('CLOTH-001', 'Cotton T-Shirt', 'Comfortable cotton t-shirt', 19.99, 200, 2, 'FashionCo', 'https://example.com/tshirt.jpg', 'ACTIVE'),
('CLOTH-002', 'Denim Jeans', 'Classic fit denim jeans', 59.99, 150, 2, 'FashionCo', 'https://example.com/jeans.jpg', 'ACTIVE'),
('CLOTH-003', 'Running Shoes', 'Lightweight running shoes', 89.99, 80, 2, 'SportWear', 'https://example.com/shoes.jpg', 'ACTIVE'),
('BOOK-001', 'Spring Boot Guide', 'Comprehensive guide to Spring Boot', 39.99, 75, 3, 'TechBooks', 'https://example.com/springboot.jpg', 'ACTIVE'),
('BOOK-002', 'Java Programming', 'Complete Java programming reference', 49.99, 60, 3, 'TechBooks', 'https://example.com/java.jpg', 'ACTIVE'),
('HOME-001', 'LED Desk Lamp', 'Adjustable LED desk lamp', 34.99, 120, 4, 'HomeLux', 'https://example.com/lamp.jpg', 'ACTIVE'),
('HOME-002', 'Coffee Maker', '12-cup programmable coffee maker', 79.99, 45, 4, 'KitchenPro', 'https://example.com/coffee.jpg', 'ACTIVE'),
('SPORT-001', 'Yoga Mat', 'Non-slip exercise yoga mat', 24.99, 150, 5, 'FitGear', 'https://example.com/yogamat.jpg', 'ACTIVE'),
('SPORT-002', 'Dumbbell Set', '20lb adjustable dumbbell set', 129.99, 40, 5, 'FitGear', 'https://example.com/dumbbells.jpg', 'ACTIVE');

-- Insert sample user (password: Test@1234)
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, status) VALUES
('testuser', 'test@example.com', '$2a$10$XYZ123...', 'Test', 'User', '1234567890', 'ACTIVE'),
('johndoe', 'john.doe@example.com', '$2a$10$ABC456...', 'John', 'Doe', '9876543210', 'ACTIVE');

-- Insert sample addresses
INSERT INTO addresses (user_id, address_type, street_address, city, state, postal_code, country, is_default) VALUES
(1, 'SHIPPING', '123 Main Street', 'New York', 'NY', '10001', 'USA', TRUE),
(1, 'BILLING', '456 Oak Avenue', 'New York', 'NY', '10002', 'USA', FALSE),
(2, 'SHIPPING', '789 Pine Road', 'Los Angeles', 'CA', '90001', 'USA', TRUE);