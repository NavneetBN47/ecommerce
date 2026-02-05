-- Shopping Cart System DML
-- Data Manipulation Language statements for seed data
-- All statements are additive and non-destructive

-- Sample seed data for testing and validation
-- Note: In production, user_id and product_id would reference actual entities

-- Insert sample carts (using fixed UUIDs for consistency in testing)
INSERT INTO Cart (cart_id, user_id, status, created_at, updated_at)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440101', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440102', 'completed', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440103', 'abandoned', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (cart_id) DO NOTHING;

-- Insert sample cart items
INSERT INTO CartItem (item_id, cart_id, product_id, quantity, price_at_addition)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440301', 2, 29.99),
    ('550e8400-e29b-41d4-a716-446655440202', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440302', 1, 49.99),
    ('550e8400-e29b-41d4-a716-446655440203', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440303', 3, 15.99),
    ('550e8400-e29b-41d4-a716-446655440204', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440304', 1, 99.99)
ON CONFLICT (item_id) DO NOTHING;

-- Update statistics for better query planning
ANALYZE Cart;
ANALYZE CartItem;

-- Verify data integrity after inserts
DO $$
DECLARE
    cart_count INTEGER;
    item_count INTEGER;
    orphaned_items INTEGER;
BEGIN
    SELECT COUNT(*) INTO cart_count FROM Cart;
    SELECT COUNT(*) INTO item_count FROM CartItem;
    
    SELECT COUNT(*) INTO orphaned_items 
    FROM CartItem ci 
    LEFT JOIN Cart c ON ci.cart_id = c.cart_id 
    WHERE c.cart_id IS NULL;
    
    RAISE NOTICE 'Data integrity check: % carts, % items, % orphaned items', cart_count, item_count, orphaned_items;
    
    IF orphaned_items > 0 THEN
        RAISE WARNING 'Found % orphaned cart items', orphaned_items;
    END IF;
END $$;