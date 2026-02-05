-- =========================
-- VALIDATION QUERIES
-- =========================
-- Validate carts table structure
SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'carts';

-- Validate cart_items table structure
SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'cart_items';

-- Validate single active cart per user
SELECT user_id, COUNT(*) FROM carts WHERE status = 'active' GROUP BY user_id HAVING COUNT(*) > 1;

-- Check for expired carts
SELECT cart_id FROM carts WHERE expires_at < CURRENT_TIMESTAMP;

-- Cart item quantity validation
SELECT item_id FROM cart_items WHERE quantity <= 0;
