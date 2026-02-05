-- Shopping Cart System Data Manipulation Language (DML)
-- Seed data and sample records as per LLD specifications

-- Insert sample users with hashed passwords (BCrypt format)
INSERT INTO users (username, password, email, roles) VALUES
('john_doe', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZg.BqY/VXW5EkVvVdpEBBXDlJvAu', 'john.doe@example.com', 'USER'),
('jane_smith', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZg.BqY/VXW5EkVvVdpEBBXDlJvAu', 'jane.smith@example.com', 'USER'),
('admin_user', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZg.BqY/VXW5EkVvVdpEBBXDlJvAu', 'admin@example.com', 'ADMIN'),
('bob_wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZg.BqY/VXW5EkVvVdpEBBXDlJvAu', 'bob.wilson@example.com', 'USER'),
('alice_brown', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZg.BqY/VXW5EkVvVdpEBBXDlJvAu', 'alice.brown@example.com', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Insert sample products across different categories
INSERT INTO products (name, description, price, stock_quantity, category) VALUES
('Wireless Headphones', 'High-quality wireless headphones with noise cancellation', 199.99, 50, 'Electronics'),
('Gaming Mouse', 'Ergonomic gaming mouse with RGB lighting', 79.99, 75, 'Electronics'),
('Coffee Mug', 'Ceramic coffee mug with heat retention', 15.99, 200, 'Home & Kitchen'),
('Bluetooth Speaker', 'Portable Bluetooth speaker with 12-hour battery', 89.99, 30, 'Electronics'),
('Notebook Set', 'Set of 3 premium notebooks for journaling', 24.99, 100, 'Office Supplies'),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 45.99, 40, 'Home & Kitchen'),
('Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 129.99, 25, 'Electronics'),
('Water Bottle', 'Insulated stainless steel water bottle', 29.99, 150, 'Sports & Outdoors'),
('Phone Case', 'Protective phone case with drop protection', 19.99, 300, 'Electronics'),
('Yoga Mat', 'Non-slip yoga mat with carrying strap', 39.99, 60, 'Sports & Outdoors')
ON CONFLICT DO NOTHING;

-- Insert sample active carts for users
INSERT INTO cart (user_id, status) VALUES
(1, 'ACTIVE'),
(2, 'ACTIVE'),
(4, 'ACTIVE')
ON CONFLICT (user_id) DO NOTHING;

-- Insert sample cart items with price at addition
INSERT INTO cart_items (cart_id, product_id, quantity, price_at_addition) VALUES
(1, 1, 2, 199.99),  -- john_doe: 2x Wireless Headphones
(1, 3, 1, 15.99),   -- john_doe: 1x Coffee Mug
(2, 2, 1, 79.99),   -- jane_smith: 1x Gaming Mouse
(2, 5, 3, 24.99),   -- jane_smith: 3x Notebook Set
(2, 8, 1, 29.99),   -- jane_smith: 1x Water Bottle
(3, 4, 1, 89.99),   -- bob_wilson: 1x Bluetooth Speaker
(3, 6, 2, 45.99)    -- bob_wilson: 2x Desk Lamp
ON CONFLICT (cart_id, product_id) DO NOTHING;

-- Insert a sample checked out cart for historical data
INSERT INTO cart (user_id, status) VALUES
(5, 'CHECKED_OUT')
ON CONFLICT (user_id) DO NOTHING;

-- Insert items for the checked out cart
INSERT INTO cart_items (cart_id, product_id, quantity, price_at_addition) VALUES
((SELECT id FROM cart WHERE user_id = 5), 7, 1, 129.99),  -- alice_brown: 1x Mechanical Keyboard
((SELECT id FROM cart WHERE user_id = 5), 9, 2, 19.99)    -- alice_brown: 2x Phone Case
ON CONFLICT (cart_id, product_id) DO NOTHING;
