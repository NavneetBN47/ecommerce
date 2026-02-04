-- Add missing columns and constraints to align with LLD requirements

-- Users table enhancements
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Add unique constraint on email if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'users_email_unique' 
        AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);
    END IF;
END $$;

-- Products table enhancements  
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- Add check constraint for price >= 0
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'products_price_check'
    ) THEN
        ALTER TABLE products ADD CONSTRAINT products_price_check CHECK (price >= 0);
    END IF;
END $$;

-- Add check constraint for available_qty >= 0  
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'products_qty_check'
    ) THEN
        ALTER TABLE products ADD CONSTRAINT products_qty_check CHECK (available_qty >= 0);
    END IF;
END $$;

-- Cart table enhancements
ALTER TABLE cart 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create trigger to auto-update updated_at
CREATE OR REPLACE FUNCTION update_cart_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS cart_update_trigger ON cart;
CREATE TRIGGER cart_update_trigger
    BEFORE UPDATE ON cart
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_timestamp();

-- CartItem table enhancements
ALTER TABLE cart_items 
ADD COLUMN IF NOT EXISTS price_at_addition DECIMAL(10,2);

-- Add check constraint for quantity > 0
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.check_constraints 
        WHERE constraint_name = 'cart_items_qty_check'
    ) THEN
        ALTER TABLE cart_items ADD CONSTRAINT cart_items_qty_check CHECK (quantity > 0);
    END IF;
END $$;