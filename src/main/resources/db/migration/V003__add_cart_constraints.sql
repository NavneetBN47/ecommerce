-- V003: Add additional constraints and indexes for cart management
-- Author: Backend Automation Agent
-- Date: 2024
-- Purpose: Enhance cart functionality with proper constraints for lazy creation and auto-delete

-- Add check constraint for cart item quantity
ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0);

-- Add check constraint for cart item price
ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_item_price CHECK (price >= 0);

-- Add check constraint for cart item subtotal
ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_item_subtotal CHECK (subtotal >= 0);

-- Add check constraint for cart total amount
ALTER TABLE carts
ADD CONSTRAINT chk_cart_total_amount CHECK (total_amount >= 0);

-- Add check constraint for product stock quantity
ALTER TABLE products
ADD CONSTRAINT chk_product_stock_quantity CHECK (stock_quantity >= 0);

-- Add check constraint for product price
ALTER TABLE products
ADD CONSTRAINT chk_product_price CHECK (price >= 0);

-- Add check constraint for order item quantity
ALTER TABLE order_items
ADD CONSTRAINT chk_order_item_quantity CHECK (quantity > 0);

-- Add check constraint for order total amount
ALTER TABLE orders
ADD CONSTRAINT chk_order_total_amount CHECK (total_amount >= 0);

-- Add composite index for better cart item lookup performance
CREATE INDEX idx_cart_items_cart_product ON cart_items(cart_id, product_id);

-- Add index for order date range queries
CREATE INDEX idx_orders_date ON orders(order_date);

-- Add index for user orders lookup
CREATE INDEX idx_orders_user_date ON orders(user_id, order_date DESC);