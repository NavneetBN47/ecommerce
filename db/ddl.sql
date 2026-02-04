-- =========================
-- SCHEMA RECONCILIATION SCRIPT
-- Adding missing constraints based on LLD requirements
-- =========================

-- Add quantity constraint to ensure quantity > 0
ALTER TABLE cart_items 
ADD CONSTRAINT chk_cart_items_quantity_positive 
CHECK (quantity > 0);

-- Add price constraint to ensure price >= 0
ALTER TABLE products 
ADD CONSTRAINT chk_products_price_non_negative 
CHECK (price >= 0);

-- Add available_qty constraint to ensure available_qty >= 0
ALTER TABLE products 
ADD CONSTRAINT chk_products_available_qty_non_negative 
CHECK (available_qty >= 0);

-- Drop existing foreign key constraint and recreate with CASCADE DELETE
ALTER TABLE cart_items 
DROP CONSTRAINT IF EXISTS fk_cart_items_cart;

ALTER TABLE cart_items 
ADD CONSTRAINT fk_cart_items_cart 
FOREIGN KEY (cart_id) 
REFERENCES cart(cart_id) 
ON DELETE CASCADE;

-- Add index for better performance on cart lookups
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);

-- Add index for better performance on cart_items lookups
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);

-- Add index for product search performance
CREATE INDEX IF NOT EXISTS idx_products_name_search ON products USING gin(to_tsvector('english', product_name));