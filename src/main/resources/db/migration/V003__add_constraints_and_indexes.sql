-- Additional Constraints and Indexes for Performance Optimization

-- Add check constraints
ALTER TABLE products
ADD CONSTRAINT chk_product_price CHECK (price > 0),
ADD CONSTRAINT chk_product_stock CHECK (stock_quantity >= 0);

ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0),
ADD CONSTRAINT chk_cart_item_price CHECK (unit_price >= 0);

ALTER TABLE order_items
ADD CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
ADD CONSTRAINT chk_order_item_price CHECK (unit_price >= 0);

-- Add composite indexes for common queries
CREATE INDEX idx_product_category_active ON products(category, active);
CREATE INDEX idx_product_brand_active ON products(brand, active);
CREATE INDEX idx_product_featured_active ON products(featured, active);
CREATE INDEX idx_order_user_status ON orders(user_id, status);
CREATE INDEX idx_order_user_date ON orders(user_id, order_date DESC);

-- Add full-text index for product search
CREATE FULLTEXT INDEX idx_product_search ON products(name, description);