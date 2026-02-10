-- Migration V004: Reconcile CartItem Schema with LLD Requirements
-- Ensures quantity constraint and proper cascading

-- Add check constraint for quantity > 0 (LLD requirement)
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS chk_cart_item_quantity_positive;
ALTER TABLE cart_items ADD CONSTRAINT chk_cart_item_quantity_positive CHECK (quantity > 0);

-- Ensure proper foreign key cascading for cart deletion
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS cart_items_cart_id_fkey;
ALTER TABLE cart_items ADD CONSTRAINT cart_items_cart_id_fkey 
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(cart_id) ON DELETE CASCADE;

-- Ensure proper foreign key constraint for product
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS cart_items_product_id_fkey;
ALTER TABLE cart_items ADD CONSTRAINT cart_items_product_id_fkey 
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE;

-- Add comment to document schema reconciliation
COMMENT ON CONSTRAINT chk_cart_item_quantity_positive ON cart_items IS 'Quantity must be greater than 0 (LLD requirement)';