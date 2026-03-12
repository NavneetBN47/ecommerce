-- =========================
-- MIGRATION V002: Add Indexes
-- Created: 2026-03-12
-- Description: Adds performance indexes for frequently queried columns
-- =========================

-- Index on username for login queries
CREATE INDEX idx_users_username ON users(username);

-- Index on product_name for search queries (case-insensitive)
CREATE INDEX idx_products_name_lower ON products(LOWER(product_name));

-- Index on available_qty for availability checks
CREATE INDEX idx_products_available_qty ON products(available_qty);

-- Index on cart user_id for cart lookups
CREATE INDEX idx_cart_user_id ON cart(user_id);

-- Composite index on cart_items for cart operations
CREATE INDEX idx_cart_items_cart_product ON cart_items(cart_id, product_id);