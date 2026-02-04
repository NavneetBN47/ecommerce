-- =========================
-- ADDITIONAL CONSTRAINTS FOR SHOPPING CART SYSTEM
-- =========================
-- Adding missing constraints based on LLD requirements

-- Add constraint to ensure product price is non-negative
ALTER TABLE products 
ADD CONSTRAINT chk_products_price_non_negative 
CHECK (price >= 0);

-- Add constraint to ensure product available_qty is non-negative
ALTER TABLE products 
ADD CONSTRAINT chk_products_available_qty_non_negative 
CHECK (available_qty >= 0);

-- Add constraint to ensure cart_items quantity is positive
ALTER TABLE cart_items 
ADD CONSTRAINT chk_cart_items_quantity_positive 
CHECK (quantity > 0);

-- Add unique constraint on cart_id and product_id combination to prevent duplicate products in same cart
ALTER TABLE cart_items 
ADD CONSTRAINT uk_cart_items_cart_product 
UNIQUE (cart_id, product_id);