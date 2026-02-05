-- =========================
-- SEED DATA
-- =========================
INSERT INTO carts (cart_id, user_id, created_at, updated_at, status, expires_at) VALUES
('11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'active', CURRENT_TIMESTAMP + INTERVAL '30 minutes');

INSERT INTO cart_items (item_id, cart_id, product_id, quantity, price_at_addition) VALUES
('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', 2, 1200.00);

-- =========================
-- SYSTEM CONFIGURATION
-- =========================
CREATE TABLE cart_config (
    config_id SERIAL PRIMARY KEY,
    param_key VARCHAR(50) UNIQUE NOT NULL,
    param_value VARCHAR(100) NOT NULL
);
INSERT INTO cart_config (param_key, param_value) VALUES ('cart_expiry_minutes', '30');
