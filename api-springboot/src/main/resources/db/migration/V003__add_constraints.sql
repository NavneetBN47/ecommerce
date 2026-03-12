-- =========================
-- MIGRATION V003: Add Constraints
-- Created: 2026-03-12
-- Description: Adds business rule constraints and cascades
-- =========================

-- Add check constraint for positive price
ALTER TABLE products
ADD CONSTRAINT chk_products_price_positive
CHECK (price > 0);

-- Add check constraint for non-negative quantity
ALTER TABLE products
ADD CONSTRAINT chk_products_qty_non_negative
CHECK (available_qty >= 0);

-- Add check constraint for positive cart item quantity
ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_items_qty_positive
CHECK (quantity > 0);

-- Add cascade delete for cart items when cart is deleted
ALTER TABLE cart_items
DROP CONSTRAINT IF EXISTS fk_cart_items_cart,
ADD CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id)
    REFERENCES cart(cart_id)
    ON DELETE CASCADE;

-- Add unique constraint to prevent duplicate products in same cart
ALTER TABLE cart_items
ADD CONSTRAINT uq_cart_items_cart_product
UNIQUE (cart_id, product_id);