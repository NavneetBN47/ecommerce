-- Shopping Cart System - Data Manipulation Language (DML)
-- Seed data and required mutations as per LLD
-- Version: 1.0
-- Date: $(date)

-- Insert sample product categories
INSERT INTO product_categories (category_id, name, description) VALUES
    (uuid_generate_v4(), 'Electronics', 'Electronic devices and accessories'),
    (uuid_generate_v4(), 'Clothing', 'Apparel and fashion items'),
    (uuid_generate_v4(), 'Books', 'Books and educational materials'),
    (uuid_generate_v4(), 'Home & Garden', 'Home improvement and garden supplies'),
    (uuid_generate_v4(), 'Sports & Outdoors', 'Sports equipment and outdoor gear')
ON CONFLICT (name) DO NOTHING;

-- Insert sample users
INSERT INTO users (user_id, username, email, password_hash, first_name, last_name, phone, date_of_birth) VALUES
    (uuid_generate_v4(), 'john_doe', 'john.doe@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3bp.Gm.F5e', 'John', 'Doe', '+1-555-0101', '1990-05-15'),
    (uuid_generate_v4(), 'jane_smith', 'jane.smith@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3bp.Gm.F5e', 'Jane', 'Smith', '+1-555-0102', '1985-08-22'),
    (uuid_generate_v4(), 'bob_wilson', 'bob.wilson@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3bp.Gm.F5e', 'Bob', 'Wilson', '+1-555-0103', '1992-12-03'),
    (uuid_generate_v4(), 'alice_brown', 'alice.brown@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3bp.Gm.F5e', 'Alice', 'Brown', '+1-555-0104', '1988-03-17'),
    (uuid_generate_v4(), 'charlie_davis', 'charlie.davis@example.com', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3bp.Gm.F5e', 'Charlie', 'Davis', '+1-555-0105', '1995-07-09')
ON CONFLICT (email) DO NOTHING;

-- Insert sample products
INSERT INTO products (product_id, name, description, price, category, brand, sku, stock_quantity, weight, dimensions, image_urls) VALUES
    (uuid_generate_v4(), 'Wireless Bluetooth Headphones', 'High-quality wireless headphones with noise cancellation', 199.99, 'Electronics', 'TechBrand', 'TB-WH-001', 50, 0.350, '{"length": 20, "width": 18, "height": 8}', ARRAY['https://example.com/images/headphones1.jpg', 'https://example.com/images/headphones2.jpg']),
    (uuid_generate_v4(), 'Smartphone Case', 'Protective case for smartphones with shock absorption', 29.99, 'Electronics', 'ProtectPro', 'PP-SC-002', 200, 0.100, '{"length": 15, "width": 8, "height": 1}', ARRAY['https://example.com/images/case1.jpg']),
    (uuid_generate_v4(), 'Cotton T-Shirt', 'Comfortable 100% cotton t-shirt in various colors', 24.99, 'Clothing', 'ComfortWear', 'CW-TS-003', 100, 0.200, '{"size": "M"}', ARRAY['https://example.com/images/tshirt1.jpg', 'https://example.com/images/tshirt2.jpg']),
    (uuid_generate_v4(), 'Programming Book: Clean Code', 'Essential book for software developers', 45.00, 'Books', 'TechPublisher', 'TP-CC-004', 25, 0.800, '{"pages": 464}', ARRAY['https://example.com/images/cleancode.jpg']),
    (uuid_generate_v4(), 'Yoga Mat', 'Non-slip yoga mat for exercise and meditation', 35.99, 'Sports & Outdoors', 'FitLife', 'FL-YM-005', 75, 1.200, '{"length": 183, "width": 61, "thickness": 6}', ARRAY['https://example.com/images/yogamat1.jpg']),
    (uuid_generate_v4(), 'Coffee Maker', 'Automatic drip coffee maker with programmable timer', 89.99, 'Home & Garden', 'BrewMaster', 'BM-CM-006', 30, 2.500, '{"length": 25, "width": 20, "height": 35}', ARRAY['https://example.com/images/coffeemaker1.jpg']),
    (uuid_generate_v4(), 'Running Shoes', 'Lightweight running shoes with cushioned sole', 129.99, 'Sports & Outdoors', 'RunFast', 'RF-RS-007', 60, 0.600, '{"size": "10"}', ARRAY['https://example.com/images/shoes1.jpg', 'https://example.com/images/shoes2.jpg']),
    (uuid_generate_v4(), 'Desk Lamp', 'LED desk lamp with adjustable brightness', 49.99, 'Home & Garden', 'LightPro', 'LP-DL-008', 40, 1.100, '{"height": 45, "base_diameter": 15}', ARRAY['https://example.com/images/desklamp1.jpg']),
    (uuid_generate_v4(), 'Wireless Mouse', 'Ergonomic wireless mouse with long battery life', 39.99, 'Electronics', 'TechBrand', 'TB-WM-009', 80, 0.150, '{"length": 12, "width": 6, "height": 4}', ARRAY['https://example.com/images/mouse1.jpg']),
    (uuid_generate_v4(), 'Cookbook: Healthy Recipes', 'Collection of nutritious and delicious recipes', 32.50, 'Books', 'HealthyLife', 'HL-HR-010', 35, 0.700, '{"pages": 320}', ARRAY['https://example.com/images/cookbook1.jpg'])
ON CONFLICT (sku) DO NOTHING;

-- Insert sample user addresses
INSERT INTO user_addresses (address_id, user_id, address_type, street_address, city, state, postal_code, country, is_default)
SELECT 
    uuid_generate_v4(),
    u.user_id,
    'shipping',
    CASE 
        WHEN u.username = 'john_doe' THEN '123 Main Street'
        WHEN u.username = 'jane_smith' THEN '456 Oak Avenue'
        WHEN u.username = 'bob_wilson' THEN '789 Pine Road'
        WHEN u.username = 'alice_brown' THEN '321 Elm Street'
        WHEN u.username = 'charlie_davis' THEN '654 Maple Drive'
    END,
    CASE 
        WHEN u.username = 'john_doe' THEN 'New York'
        WHEN u.username = 'jane_smith' THEN 'Los Angeles'
        WHEN u.username = 'bob_wilson' THEN 'Chicago'
        WHEN u.username = 'alice_brown' THEN 'Houston'
        WHEN u.username = 'charlie_davis' THEN 'Phoenix'
    END,
    CASE 
        WHEN u.username = 'john_doe' THEN 'NY'
        WHEN u.username = 'jane_smith' THEN 'CA'
        WHEN u.username = 'bob_wilson' THEN 'IL'
        WHEN u.username = 'alice_brown' THEN 'TX'
        WHEN u.username = 'charlie_davis' THEN 'AZ'
    END,
    CASE 
        WHEN u.username = 'john_doe' THEN '10001'
        WHEN u.username = 'jane_smith' THEN '90210'
        WHEN u.username = 'bob_wilson' THEN '60601'
        WHEN u.username = 'alice_brown' THEN '77001'
        WHEN u.username = 'charlie_davis' THEN '85001'
    END,
    'US',
    true
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM user_addresses ua 
    WHERE ua.user_id = u.user_id AND ua.address_type = 'shipping'
);

-- Insert sample carts (some active, some abandoned)
INSERT INTO carts (cart_id, user_id, status, expires_at)
SELECT 
    uuid_generate_v4(),
    u.user_id,
    CASE 
        WHEN u.username IN ('john_doe', 'jane_smith') THEN 'active'
        WHEN u.username = 'bob_wilson' THEN 'abandoned'
        ELSE 'active'
    END,
    CURRENT_TIMESTAMP + INTERVAL '30 days'
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM carts c 
    WHERE c.user_id = u.user_id AND c.status = 'active'
);

-- Insert sample cart items
WITH cart_product_combinations AS (
    SELECT 
        c.cart_id,
        p.product_id,
        p.price,
        CASE 
            WHEN c.user_id = (SELECT user_id FROM users WHERE username = 'john_doe') AND p.sku = 'TB-WH-001' THEN 1
            WHEN c.user_id = (SELECT user_id FROM users WHERE username = 'john_doe') AND p.sku = 'CW-TS-003' THEN 2
            WHEN c.user_id = (SELECT user_id FROM users WHERE username = 'jane_smith') AND p.sku = 'FL-YM-005' THEN 1
            WHEN c.user_id = (SELECT user_id FROM users WHERE username = 'jane_smith') AND p.sku = 'RF-RS-007' THEN 1
            WHEN c.user_id = (SELECT user_id FROM users WHERE username = 'bob_wilson') AND p.sku = 'TP-CC-004' THEN 1
            ELSE NULL
        END as quantity
    FROM carts c
    CROSS JOIN products p
    WHERE c.status = 'active'
)
INSERT INTO cart_items (cart_item_id, cart_id, product_id, quantity, unit_price, total_price)
SELECT 
    uuid_generate_v4(),
    cpc.cart_id,
    cpc.product_id,
    cpc.quantity,
    cpc.price,
    cpc.price * cpc.quantity
FROM cart_product_combinations cpc
WHERE cpc.quantity IS NOT NULL
ON CONFLICT (cart_id, product_id) DO NOTHING;

-- Update product stock after adding items to carts
UPDATE products 
SET stock_quantity = stock_quantity - (
    SELECT COALESCE(SUM(ci.quantity), 0)
    FROM cart_items ci
    WHERE ci.product_id = products.product_id
)
WHERE product_id IN (
    SELECT DISTINCT product_id FROM cart_items
);

-- Insert additional sample data for testing edge cases

-- Add a user with multiple addresses
DO $$
DECLARE
    test_user_id UUID;
BEGIN
    -- Insert test user
    INSERT INTO users (user_id, username, email, password_hash, first_name, last_name)
    VALUES (uuid_generate_v4(), 'test_user', 'test@example.com', '$2b$12$test', 'Test', 'User')
    ON CONFLICT (email) DO NOTHING
    RETURNING user_id INTO test_user_id;
    
    -- If user was inserted, add multiple addresses
    IF test_user_id IS NOT NULL THEN
        INSERT INTO user_addresses (user_id, address_type, street_address, city, state, postal_code, is_default)
        VALUES 
            (test_user_id, 'shipping', '123 Test St', 'Test City', 'TS', '12345', true),
            (test_user_id, 'billing', '456 Bill Ave', 'Bill City', 'BC', '67890', false);
    END IF;
END $$;

-- Add products with zero stock for testing
INSERT INTO products (name, description, price, category, brand, sku, stock_quantity)
VALUES 
    ('Out of Stock Item', 'This item is currently out of stock', 99.99, 'Electronics', 'TestBrand', 'TB-OOS-999', 0),
    ('Limited Stock Item', 'This item has very limited stock', 149.99, 'Electronics', 'TestBrand', 'TB-LS-998', 1)
ON CONFLICT (sku) DO NOTHING;

-- Create some expired carts for testing
INSERT INTO carts (user_id, status, expires_at, created_at)
SELECT 
    u.user_id,
    'expired',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '31 days'
FROM users u
WHERE u.username = 'charlie_davis'
AND NOT EXISTS (
    SELECT 1 FROM carts c 
    WHERE c.user_id = u.user_id AND c.status = 'expired'
);

-- Commit all changes
COMMIT;

-- Verify data integrity
DO $$
BEGIN
    -- Check that all carts have correct totals
    IF EXISTS (
        SELECT 1 FROM carts c
        WHERE c.total_amount != (
            SELECT COALESCE(SUM(ci.total_price), 0)
            FROM cart_items ci
            WHERE ci.cart_id = c.cart_id
        )
    ) THEN
        RAISE EXCEPTION 'Cart totals are inconsistent with cart items';
    END IF;
    
    -- Check that all cart items have positive quantities
    IF EXISTS (SELECT 1 FROM cart_items WHERE quantity <= 0) THEN
        RAISE EXCEPTION 'Found cart items with non-positive quantities';
    END IF;
    
    -- Check that all users have at least one address
    IF EXISTS (
        SELECT 1 FROM users u
        WHERE NOT EXISTS (
            SELECT 1 FROM user_addresses ua
            WHERE ua.user_id = u.user_id
        )
    ) THEN
        RAISE WARNING 'Some users do not have addresses';
    END IF;
    
    RAISE NOTICE 'Data integrity checks passed successfully';
END $$;

-- DML operations complete
