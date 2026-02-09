-- Migration script to add constraints for cart management per LLD requirements

-- Ensure one cart per user (unique constraint)
CREATE UNIQUE INDEX IF NOT EXISTS idx_shopping_carts_user_id_unique 
ON shopping_carts(user_id) 
WHERE user_id IS NOT NULL;

-- Add constraint to ensure cart items have positive quantity
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS cart_items_quantity_check;
ALTER TABLE cart_items ADD CONSTRAINT cart_items_quantity_check CHECK (quantity > 0);

-- Add index for cart cleanup operations
CREATE INDEX IF NOT EXISTS idx_shopping_carts_expires_at ON shopping_carts(expires_at);

-- Add comments
COMMENT ON CONSTRAINT idx_shopping_carts_user_id_unique ON shopping_carts IS 'Ensures one active cart per user per LLD requirement';
COMMENT ON CONSTRAINT cart_items_quantity_check ON cart_items IS 'Ensures cart item quantity is always positive per LLD requirement';