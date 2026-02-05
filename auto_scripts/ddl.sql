-- Shopping Cart System DDL
-- Generated from Low-Level Design (LLD) - Authoritative Source
-- All statements are additive and non-destructive

-- Create extension for UUID generation if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Cart table as per LLD specification
CREATE TABLE IF NOT EXISTS Cart (
    cart_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    
    -- Constraints
    CONSTRAINT chk_cart_status CHECK (status IN ('active', 'abandoned', 'completed', 'expired'))
);

-- CartItem table as per LLD specification
CREATE TABLE IF NOT EXISTS CartItem (
    item_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    price_at_addition DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES Cart(cart_id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_cartitem_quantity CHECK (quantity > 0),
    CONSTRAINT chk_cartitem_price CHECK (price_at_addition >= 0)
);

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON Cart(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_status ON Cart(status);
CREATE INDEX IF NOT EXISTS idx_cart_created_at ON Cart(created_at);
CREATE INDEX IF NOT EXISTS idx_cartitem_cart_id ON CartItem(cart_id);
CREATE INDEX IF NOT EXISTS idx_cartitem_product_id ON CartItem(product_id);

-- Trigger function for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for Cart table updated_at
DROP TRIGGER IF EXISTS update_cart_updated_at ON Cart;
CREATE TRIGGER update_cart_updated_at
    BEFORE UPDATE ON Cart
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE Cart IS 'Shopping cart entity as per LLD specification';
COMMENT ON COLUMN Cart.cart_id IS 'Unique identifier for cart (UUID)';
COMMENT ON COLUMN Cart.user_id IS 'Reference to user who owns the cart';
COMMENT ON COLUMN Cart.status IS 'Current status of the cart';

COMMENT ON TABLE CartItem IS 'Cart item entity as per LLD specification';
COMMENT ON COLUMN CartItem.item_id IS 'Unique identifier for cart item (UUID)';
COMMENT ON COLUMN CartItem.cart_id IS 'Reference to parent cart';
COMMENT ON COLUMN CartItem.product_id IS 'Reference to external Product entity';
COMMENT ON COLUMN CartItem.quantity IS 'Quantity of the product in cart';
COMMENT ON COLUMN CartItem.price_at_addition IS 'Price of product when added to cart';