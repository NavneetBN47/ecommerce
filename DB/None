-- Database Schema Reconciliation - DDL Changes
-- Date: 2026-02-04
-- Purpose: Add missing constraints based on LLD analysis

-- Add price constraint to products table
ALTER TABLE products 
ADD CONSTRAINT chk_products_price CHECK (price >= 0);

-- Add available_qty constraint to products table
ALTER TABLE products 
ADD CONSTRAINT chk_products_available_qty CHECK (available_qty >= 0);

-- Add quantity constraint to cart_items table
ALTER TABLE cart_items 
ADD CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0);

-- Add unique constraint on cart_id and product_id combination in cart_items
ALTER TABLE cart_items 
ADD CONSTRAINT uq_cart_items_cart_product UNIQUE (cart_id, product_id);

-- Add CASCADE DELETE for cart_items when cart is deleted
ALTER TABLE cart_items 
DROP CONSTRAINT IF EXISTS fk_cart_items_cart;

ALTER TABLE cart_items 
ADD CONSTRAINT fk_cart_items_cart 
FOREIGN KEY (cart_id) 
REFERENCES carts(cart_id) 
ON DELETE CASCADE;

-- Add CASCADE DELETE for cart_items when product is deleted
ALTER TABLE cart_items 
DROP CONSTRAINT IF EXISTS fk_cart_items_product;

ALTER TABLE cart_items 
ADD CONSTRAINT fk_cart_items_product 
FOREIGN KEY (product_id) 
REFERENCES products(product_id) 
ON DELETE CASCADE;

-- Schema reconciliation complete
