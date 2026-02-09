-- Migration V001: Initial Schema Alignment with LLD
-- Aligns existing database schema with Low-Level Design specifications
-- Adds missing fields, constraints, and indexes

-- ============================================
-- 1. USER TABLE ALIGNMENT
-- ============================================

-- Add username field (CRITICAL - required by LLD)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS username VARCHAR(255);

-- Populate username from email for existing users (temporary solution)
UPDATE users 
SET username = LOWER(SPLIT_PART(email, '@', 1)) 
WHERE username IS NULL;

-- Make username NOT NULL and UNIQUE
ALTER TABLE users 
ALTER COLUMN username SET NOT NULL;

ALTER TABLE users 
ADD CONSTRAINT users_username_unique UNIQUE (username);

-- Add full_name as computed column from first_name and last_name
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS full_name VARCHAR(500) 
GENERATED ALWAYS AS (first_name || ' ' || last_name) STORED;

-- Ensure email is unique
ALTER TABLE users 
ADD CONSTRAINT users_email_unique UNIQUE (email);

-- Create trigger to prevent username updates (immutability)
CREATE OR REPLACE FUNCTION prevent_username_update()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.username IS DISTINCT FROM NEW.username THEN
        RAISE EXCEPTION 'username is immutable and cannot be changed';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS username_immutable ON users;
CREATE TRIGGER username_immutable
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION prevent_username_update();

-- Create indexes for users
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ============================================
-- 2. PRODUCT TABLE ALIGNMENT
-- ============================================

-- Add available_qty field (CRITICAL - required by LLD)
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS available_qty INTEGER;

-- Populate available_qty from product_inventory for existing products
UPDATE products p
SET available_qty = pi.quantity_available
FROM product_inventory pi
WHERE p.product_id = pi.product_id
AND p.available_qty IS NULL;

-- Set default value for products without inventory
UPDATE products 
SET available_qty = 0 
WHERE available_qty IS NULL;

-- Make available_qty NOT NULL
ALTER TABLE products 
ALTER COLUMN available_qty SET NOT NULL;

-- Add check constraints
ALTER TABLE products 
ADD CONSTRAINT products_price_non_negative CHECK (price >= 0);

ALTER TABLE products 
ADD CONSTRAINT products_available_qty_non_negative CHECK (available_qty >= 0);

-- Create case-insensitive search index
CREATE INDEX IF NOT EXISTS idx_products_name_lower ON products(LOWER(name));
CREATE INDEX IF NOT EXISTS idx_products_description_lower ON products(LOWER(description));

-- ============================================
-- 3. SHOPPING CART TABLE ALIGNMENT
-- ============================================

-- Remove session_id (violates stateless requirement)
ALTER TABLE shopping_carts 
DROP COLUMN IF EXISTS session_id;

-- Remove expires_at (not in LLD)
ALTER TABLE shopping_carts 
DROP COLUMN IF EXISTS expires_at;

-- Remove updated_at (not in LLD)
ALTER TABLE shopping_carts 
DROP COLUMN IF EXISTS updated_at;

-- Add UNIQUE constraint on user_id for 1:1 relationship (CRITICAL)
ALTER TABLE shopping_carts 
ADD CONSTRAINT shopping_carts_user_id_unique UNIQUE (user_id);

-- Ensure foreign key with proper cascade
ALTER TABLE shopping_carts 
DROP CONSTRAINT IF EXISTS shopping_carts_user_id_fkey;

ALTER TABLE shopping_carts 
ADD CONSTRAINT shopping_carts_user_id_fkey 
FOREIGN KEY (user_id) REFERENCES users(user_id) 
ON DELETE CASCADE;

-- Create trigger for auto-delete empty carts
CREATE OR REPLACE FUNCTION delete_empty_carts()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM shopping_carts 
    WHERE cart_id = OLD.cart_id 
    AND NOT EXISTS (
        SELECT 1 FROM cart_items WHERE cart_id = OLD.cart_id
    );
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS auto_delete_empty_cart ON cart_items;
CREATE TRIGGER auto_delete_empty_cart
AFTER DELETE ON cart_items
FOR EACH ROW
EXECUTE FUNCTION delete_empty_carts();

-- Create unique index
CREATE UNIQUE INDEX IF NOT EXISTS idx_shopping_carts_user_id ON shopping_carts(user_id);

-- ============================================
-- 4. CART ITEMS TABLE ALIGNMENT
-- ============================================

-- Add check constraint for quantity > 0 (not >=0)
ALTER TABLE cart_items 
DROP CONSTRAINT IF EXISTS cart_items_quantity_check;

ALTER TABLE cart_items 
ADD CONSTRAINT cart_items_quantity_positive CHECK (quantity > 0);

-- Add composite unique constraint to prevent duplicate products
ALTER TABLE cart_items 
ADD CONSTRAINT cart_items_cart_product_unique 
UNIQUE (cart_id, product_id);

-- Ensure foreign keys with proper cascade
ALTER TABLE cart_items 
DROP CONSTRAINT IF EXISTS cart_items_cart_id_fkey;

ALTER TABLE cart_items 
ADD CONSTRAINT cart_items_cart_id_fkey 
FOREIGN KEY (cart_id) REFERENCES shopping_carts(cart_id) 
ON DELETE CASCADE;

ALTER TABLE cart_items 
DROP CONSTRAINT IF EXISTS cart_items_product_id_fkey;

ALTER TABLE cart_items 
ADD CONSTRAINT cart_items_product_id_fkey 
FOREIGN KEY (product_id) REFERENCES products(product_id) 
ON DELETE RESTRICT;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_cart_items_cart_product ON cart_items(cart_id, product_id);

-- ============================================
-- 5. DATA VALIDATION
-- ============================================

-- Validate all constraints are in place
DO $$
BEGIN
    -- Check username uniqueness
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'users_username_unique'
    ) THEN
        RAISE EXCEPTION 'Username unique constraint not created';
    END IF;
    
    -- Check cart user_id uniqueness
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'shopping_carts_user_id_unique'
    ) THEN
        RAISE EXCEPTION 'Cart user_id unique constraint not created';
    END IF;
    
    -- Check cart_items quantity constraint
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'cart_items_quantity_positive'
    ) THEN
        RAISE EXCEPTION 'Cart items quantity constraint not created';
    END IF;
    
    RAISE NOTICE 'All critical constraints validated successfully';
END $$;

-- ============================================
-- 6. COMMENTS FOR DOCUMENTATION
-- ============================================

COMMENT ON COLUMN users.username IS 'Unique, immutable username for login (LLD requirement)';
COMMENT ON COLUMN users.full_name IS 'Computed full name from first_name and last_name';
COMMENT ON COLUMN products.available_qty IS 'Available quantity for purchase (LLD requirement)';
COMMENT ON CONSTRAINT shopping_carts_user_id_unique ON shopping_carts IS 'Enforces 1:1 relationship between user and cart (LLD requirement)';
COMMENT ON CONSTRAINT cart_items_quantity_positive ON cart_items IS 'Ensures quantity is always greater than 0 (LLD requirement)';
COMMENT ON CONSTRAINT cart_items_cart_product_unique ON cart_items IS 'Prevents duplicate products in same cart (LLD requirement)';

-- Migration completed successfully
RAISE NOTICE 'Migration V001 completed: Schema aligned with LLD specifications';