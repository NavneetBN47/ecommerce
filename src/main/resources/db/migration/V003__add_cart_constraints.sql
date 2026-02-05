-- V003__add_cart_constraints.sql
-- Additional constraints and indexes for cart optimization

-- Add check constraint for cart item quantity
ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0);

-- Add check constraint for product stock quantity
ALTER TABLE products
ADD CONSTRAINT chk_product_stock CHECK (stock_quantity >= 0);

-- Add check constraint for product price
ALTER TABLE products
ADD CONSTRAINT chk_product_price CHECK (price >= 0);

-- Add check constraint for cart totals
ALTER TABLE carts
ADD CONSTRAINT chk_cart_total_amount CHECK (total_amount >= 0),
ADD CONSTRAINT chk_cart_total_items CHECK (total_items >= 0);

-- Add composite index for product search optimization
CREATE INDEX idx_product_search ON products(name, category, is_deleted);

-- Add index for cart item lookup optimization
CREATE INDEX idx_cart_item_lookup ON cart_items(cart_id, product_id, quantity);