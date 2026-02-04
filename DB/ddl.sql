-- =========================
-- SCHEMA RECONCILIATION SCRIPT
-- Adding missing constraints from LLD
-- =========================

-- Add CHECK constraint for cart_items quantity > 0
ALTER TABLE cart_items 
ADD CONSTRAINT chk_cart_items_quantity_positive 
CHECK (quantity > 0);

-- Add CASCADE DELETE from cart to cart_items
ALTER TABLE cart_items 
DROP CONSTRAINT IF EXISTS fk_cart_items_cart;

ALTER TABLE cart_items 
ADD CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id)
    REFERENCES cart(cart_id)
    ON DELETE CASCADE;

-- Add CHECK constraint for products price >= 0
ALTER TABLE products 
ADD CONSTRAINT chk_products_price_non_negative 
CHECK (price >= 0);

-- Add CHECK constraint for products available_qty >= 0
ALTER TABLE products 
ADD CONSTRAINT chk_products_qty_non_negative 
CHECK (available_qty >= 0);

-- Ensure username is not null and has proper constraints
ALTER TABLE users 
ALTER COLUMN username SET NOT NULL;

ALTER TABLE users 
ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE users 
ALTER COLUMN email SET NOT NULL;

-- Ensure product constraints
ALTER TABLE products 
ALTER COLUMN product_name SET NOT NULL;

ALTER TABLE products 
ALTER COLUMN price SET NOT NULL;

ALTER TABLE products 
ALTER COLUMN available_qty SET NOT NULL;

-- Ensure cart constraints
ALTER TABLE cart 
ALTER COLUMN user_id SET NOT NULL;

-- Ensure cart_items constraints
ALTER TABLE cart_items 
ALTER COLUMN cart_id SET NOT NULL;

ALTER TABLE cart_items 
ALTER COLUMN product_id SET NOT NULL;

ALTER TABLE cart_items 
ALTER COLUMN quantity SET NOT NULL;