-- V002__insert_seed_data.sql
-- Seed data for testing and development

-- Insert test users (password: 'password123' - BCrypt encoded)
INSERT INTO users (username, email, password, first_name, last_name, phone_number, active, role) VALUES
('admin', 'admin@ecommerce.com', '$2a$10$XPTYHRQfZKMZ5Yq5Z5Z5Z.Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5u', 'Admin', 'User', '1234567890', TRUE, 'ADMIN'),
('john_doe', 'john@example.com', '$2a$10$XPTYHRQfZKMZ5Yq5Z5Z5Z.Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5u', 'John', 'Doe', '9876543210', TRUE, 'CUSTOMER'),
('jane_smith', 'jane@example.com', '$2a$10$XPTYHRQfZKMZ5Yq5Z5Z5Z.Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5u', 'Jane', 'Smith', '5551234567', TRUE, 'CUSTOMER');

-- Insert test products
INSERT INTO products (name, description, sku, price, stock_quantity, category, brand, active) VALUES
('Laptop Pro 15', 'High-performance laptop with 16GB RAM and 512GB SSD', 'LAPTOP-001', 1299.99, 50, 'Electronics', 'TechBrand', TRUE),
('Wireless Mouse', 'Ergonomic wireless mouse with 2.4GHz connectivity', 'MOUSE-001', 29.99, 200, 'Electronics', 'TechBrand', TRUE),
('USB-C Cable', 'Premium USB-C to USB-C cable, 6ft length', 'CABLE-001', 14.99, 500, 'Accessories', 'TechBrand', TRUE),
('Smartphone X', 'Latest smartphone with 128GB storage', 'PHONE-001', 899.99, 100, 'Electronics', 'PhoneCorp', TRUE),
('Bluetooth Headphones', 'Noise-cancelling wireless headphones', 'HEADPHONE-001', 199.99, 150, 'Electronics', 'AudioTech', TRUE),
('Laptop Bag', 'Durable laptop bag with multiple compartments', 'BAG-001', 49.99, 75, 'Accessories', 'BagCo', TRUE),
('Mechanical Keyboard', 'RGB mechanical gaming keyboard', 'KEYBOARD-001', 129.99, 80, 'Electronics', 'TechBrand', TRUE),
('4K Monitor', '27-inch 4K UHD monitor', 'MONITOR-001', 399.99, 40, 'Electronics', 'DisplayTech', TRUE),
('Webcam HD', '1080p HD webcam with microphone', 'WEBCAM-001', 79.99, 120, 'Electronics', 'TechBrand', TRUE),
('External SSD 1TB', 'Portable external SSD with 1TB capacity', 'SSD-001', 149.99, 90, 'Electronics', 'StoragePro', TRUE);

-- Insert test addresses
INSERT INTO addresses (user_id, street_address, city, state, postal_code, country, is_default, address_type) VALUES
(2, '123 Main Street', 'New York', 'NY', '10001', 'USA', TRUE, 'BOTH'),
(2, '456 Oak Avenue', 'Brooklyn', 'NY', '11201', 'USA', FALSE, 'SHIPPING'),
(3, '789 Pine Road', 'Los Angeles', 'CA', '90001', 'USA', TRUE, 'BOTH');