-- Seed data for E-Commerce application

-- Insert sample categories
INSERT INTO categories (name, slug, description, active, display_order) VALUES
('Electronics', 'electronics', 'Electronic devices and accessories', TRUE, 1),
('Clothing', 'clothing', 'Fashion and apparel', TRUE, 2),
('Books', 'books', 'Books and publications', TRUE, 3),
('Home & Garden', 'home-garden', 'Home and garden products', TRUE, 4),
('Sports', 'sports', 'Sports and outdoor equipment', TRUE, 5);

-- Insert sample products
INSERT INTO products (sku, name, description, price, discount_price, stock_quantity, category_id, active, featured, brand) VALUES
('ELEC001', 'Wireless Headphones', 'High-quality wireless headphones with noise cancellation', 99.99, 79.99, 50, 1, TRUE, TRUE, 'AudioTech'),
('ELEC002', 'Smartphone', 'Latest smartphone with advanced features', 699.99, NULL, 30, 1, TRUE, TRUE, 'TechBrand'),
('ELEC003', 'Laptop', 'Powerful laptop for work and gaming', 1299.99, 1199.99, 20, 1, TRUE, FALSE, 'CompuPro'),
('CLOTH001', 'T-Shirt', 'Comfortable cotton t-shirt', 19.99, NULL, 100, 2, TRUE, FALSE, 'FashionCo'),
('CLOTH002', 'Jeans', 'Classic blue jeans', 49.99, 39.99, 75, 2, TRUE, FALSE, 'DenimStyle'),
('BOOK001', 'Programming Guide', 'Comprehensive programming guide', 39.99, NULL, 40, 3, TRUE, FALSE, 'TechBooks'),
('HOME001', 'Coffee Maker', 'Automatic coffee maker', 79.99, 69.99, 25, 4, TRUE, FALSE, 'HomeBrew'),
('SPORT001', 'Yoga Mat', 'Non-slip yoga mat', 29.99, NULL, 60, 5, TRUE, FALSE, 'FitGear');

-- Insert sample user (password: password123)
INSERT INTO users (username, email, password, first_name, last_name, active, email_verified) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', TRUE, TRUE),
('jane_smith', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', TRUE, TRUE);