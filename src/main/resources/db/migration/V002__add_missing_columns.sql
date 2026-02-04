-- Add is_active column to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- Add updated_at column to cart table
ALTER TABLE cart ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add price_at_addition column to cart_items table
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS price_at_addition DECIMAL(10,2);
