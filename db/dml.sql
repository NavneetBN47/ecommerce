-- Data Manipulation Language (DML)
-- Cart Management System - Seed Data
-- Generated from Low-Level Design (LLD) requirements

-- Insert sample users for testing
INSERT INTO users (user_id, username, email, first_name, last_name) 
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'john_doe', 'john.doe@example.com', 'John', 'Doe'),
    ('550e8400-e29b-41d4-a716-446655440002', 'jane_smith', 'jane.smith@example.com', 'Jane', 'Smith'),
    ('550e8400-e29b-41d4-a716-446655440003', 'bob_wilson', 'bob.wilson@example.com', 'Bob', 'Wilson')
ON CONFLICT (user_id) DO NOTHING;

-- Insert sample products for testing
INSERT INTO products (product_id, product_name, product_description, price, stock_quantity, sku) 
VALUES 
    ('660e8400-e29b-41d4-a716-446655440001', 'Wireless Headphones', 'High-quality wireless headphones with noise cancellation', 99.99, 50, 'WH-001'),
    ('660e8400-e29b-41d4-a716-446655440002', 'Smartphone Case', 'Protective case for smartphones with multiple color options', 19.99, 100, 'SC-002'),
    ('660e8400-e29b-41d4-a716-446655440003', 'Bluetooth Speaker', 'Portable Bluetooth speaker with excellent sound quality', 79.99, 30, 'BS-003'),
    ('660e8400-e29b-41d4-a716-446655440004', 'USB-C Cable', 'High-speed USB-C charging and data cable', 12.99, 200, 'UC-004'),
    ('660e8400-e29b-41d4-a716-446655440005', 'Laptop Stand', 'Adjustable laptop stand for ergonomic working', 45.99, 25, 'LS-005')
ON CONFLICT (product_id) DO NOTHING;

-- Insert sample active carts
INSERT INTO cart (cart_id, user_id, cart_status, session_id, currency_code) 
VALUES 
    ('770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'active', 'sess_001', 'USD'),
    ('770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'active', 'sess_002', 'USD'),
    ('770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'abandoned', 'sess_003', 'USD')
ON CONFLICT (cart_id) DO NOTHING;

-- Insert sample cart items
INSERT INTO cart_item (cart_item_id, cart_id, product_id, quantity, unit_price) 
VALUES 
    ('880e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', 1, 99.99),
    ('880e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440002', 2, 19.99),
    ('880e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440003', 1, 79.99),
    ('880e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440004', 3, 12.99),
    ('880e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440005', 1, 45.99)
ON CONFLICT (cart_item_id) DO NOTHING;

-- Insert sample cart history records
INSERT INTO cart_history (history_id, cart_id, action_type, action_details, performed_by) 
VALUES 
    ('990e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 'created', '{"cart_status": "active"}', '550e8400-e29b-41d4-a716-446655440001'),
    ('990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001', 'item_added', '{"product_id": "660e8400-e29b-41d4-a716-446655440001", "quantity": 1}', '550e8400-e29b-41d4-a716-446655440001'),
    ('990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 'item_added', '{"product_id": "660e8400-e29b-41d4-a716-446655440002", "quantity": 2}', '550e8400-e29b-41d4-a716-446655440001'),
    ('990e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440002', 'created', '{"cart_status": "active"}', '550e8400-e29b-41d4-a716-446655440002'),
    ('990e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440003', 'status_changed', '{"old_status": "active", "new_status": "abandoned"}', '550e8400-e29b-41d4-a716-446655440003')
ON CONFLICT (history_id) DO NOTHING;

-- Update cart totals (triggers should handle this automatically, but ensuring consistency)
UPDATE cart 
SET 
    total_amount = (
        SELECT COALESCE(SUM(ci.total_price), 0) 
        FROM cart_item ci 
        WHERE ci.cart_id = cart.cart_id
    ),
    total_items = (
        SELECT COALESCE(SUM(ci.quantity), 0) 
        FROM cart_item ci 
        WHERE ci.cart_id = cart.cart_id
    )
WHERE cart_id IN (
    '770e8400-e29b-41d4-a716-446655440001',
    '770e8400-e29b-41d4-a716-446655440002',
    '770e8400-e29b-41d4-a716-446655440003'
);

-- Insert additional test data for edge cases

-- Test user with empty cart
INSERT INTO users (user_id, username, email, first_name, last_name) 
VALUES ('550e8400-e29b-41d4-a716-446655440004', 'test_user', 'test.user@example.com', 'Test', 'User')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO cart (cart_id, user_id, cart_status, session_id) 
VALUES ('770e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'active', 'sess_004')
ON CONFLICT (cart_id) DO NOTHING;

-- Test product with zero stock
INSERT INTO products (product_id, product_name, product_description, price, stock_quantity, sku) 
VALUES ('660e8400-e29b-41d4-a716-446655440006', 'Out of Stock Item', 'Product currently out of stock', 29.99, 0, 'OOS-006')
ON CONFLICT (product_id) DO NOTHING;

-- Test expired cart
INSERT INTO cart (cart_id, user_id, cart_status, session_id, expires_at) 
VALUES (
    '770e8400-e29b-41d4-a716-446655440005', 
    '550e8400-e29b-41d4-a716-446655440001', 
    'expired', 
    'sess_005', 
    CURRENT_TIMESTAMP - INTERVAL '1 day'
)
ON CONFLICT (cart_id) DO NOTHING;

-- Commit the transaction
COMMIT;