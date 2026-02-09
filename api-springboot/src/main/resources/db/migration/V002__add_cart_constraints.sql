-- Migration script to add additional constraints for cart management
-- Ensures one cart per user and proper cascade behavior

-- Add unique constraint on user_id in shopping_carts if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uq_shopping_carts_user_id'
    ) THEN
        ALTER TABLE shopping_carts 
        ADD CONSTRAINT uq_shopping_carts_user_id UNIQUE (user_id);
    END IF;
END $$;

-- Ensure cart items are deleted when cart is deleted (already in DDL but verify)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_cart_items_cart_cascade'
    ) THEN
        ALTER TABLE cart_items 
        DROP CONSTRAINT IF EXISTS cart_items_cart_id_fkey;
        
        ALTER TABLE cart_items 
        ADD CONSTRAINT fk_cart_items_cart_cascade 
        FOREIGN KEY (cart_id) REFERENCES shopping_carts(cart_id) 
        ON DELETE CASCADE;
    END IF;
END $$;

-- Add check constraint to ensure quantity is positive
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_cart_items_quantity_positive'
    ) THEN
        ALTER TABLE cart_items 
        ADD CONSTRAINT chk_cart_items_quantity_positive 
        CHECK (quantity > 0);
    END IF;
END $$;

-- Add comments
COMMENT ON CONSTRAINT uq_shopping_carts_user_id ON shopping_carts IS 'Ensures one active cart per user';
COMMENT ON CONSTRAINT fk_cart_items_cart_cascade ON cart_items IS 'Cascade delete cart items when cart is deleted';
COMMENT ON CONSTRAINT chk_cart_items_quantity_positive ON cart_items IS 'Ensures cart item quantity is always positive';