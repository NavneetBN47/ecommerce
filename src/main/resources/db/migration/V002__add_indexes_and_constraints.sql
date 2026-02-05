-- V002__add_indexes_and_constraints.sql
-- Additional indexes and constraints for performance optimization

-- Add composite indexes for common queries
CREATE INDEX idx_cart_user_status ON carts(user_id, status);
CREATE INDEX idx_order_user_status ON orders(user_id, status);
CREATE INDEX idx_product_category_active ON products(category, active);

-- Add index for product search (case-insensitive)
CREATE INDEX idx_product_name_lower ON products((LOWER(name)));

-- Add index for order date range queries
CREATE INDEX idx_order_date_status ON orders(order_date, status);

-- Add check constraint for cart status
ALTER TABLE carts ADD CONSTRAINT chk_cart_status 
    CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED'));

-- Add check constraint for order status
ALTER TABLE orders ADD CONSTRAINT chk_order_status 
    CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'));