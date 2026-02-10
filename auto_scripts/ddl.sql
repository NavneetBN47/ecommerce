-- Cart Management Domain - Data Definition Language (DDL)
-- Generated based on LLD specifications
-- All statements are additive and non-destructive

-- Create Cart table
CREATE TABLE IF NOT EXISTS carts (
    cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create CartItem table
CREATE TABLE IF NOT EXISTS cart_items (
    item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 1),
    CONSTRAINT chk_price_positive CHECK (price > 0)
);

-- Create Product reference table (if not exists)
CREATE TABLE IF NOT EXISTS products (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_price_positive CHECK (price > 0),
    CONSTRAINT chk_stock_non_negative CHECK (stock >= 0)
);

-- Create Users reference table (if not exists)
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_carts_user_id ON carts(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX IF NOT EXISTS idx_carts_created_at ON carts(created_at);
CREATE INDEX IF NOT EXISTS idx_cart_items_added_at ON cart_items(added_at);

-- Create trigger to update updated_at timestamp for carts
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER IF NOT EXISTS update_carts_updated_at
    BEFORE UPDATE ON carts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER IF NOT EXISTS update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER IF NOT EXISTS update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE carts IS 'Shopping carts for users';
COMMENT ON COLUMN carts.cart_id IS 'Unique identifier for the cart (UUID)';
COMMENT ON COLUMN carts.user_id IS 'Reference to the user who owns the cart';
COMMENT ON COLUMN carts.created_at IS 'Timestamp when the cart was created';
COMMENT ON COLUMN carts.updated_at IS 'Timestamp when the cart was last updated';

COMMENT ON TABLE cart_items IS 'Items within shopping carts';
COMMENT ON COLUMN cart_items.item_id IS 'Unique identifier for the cart item (UUID)';
COMMENT ON COLUMN cart_items.cart_id IS 'Reference to the parent cart';
COMMENT ON COLUMN cart_items.product_id IS 'Reference to the product';
COMMENT ON COLUMN cart_items.quantity IS 'Quantity of the product (must be >= 1)';
COMMENT ON COLUMN cart_items.price IS 'Price of the product at time of adding to cart (must be > 0)';
COMMENT ON COLUMN cart_items.added_at IS 'Timestamp when the item was added to the cart';

COMMENT ON TABLE products IS 'Product catalog reference table';
COMMENT ON COLUMN products.product_id IS 'Unique identifier for the product (UUID)';
COMMENT ON COLUMN products.name IS 'Product name';
COMMENT ON COLUMN products.price IS 'Current product price (must be > 0)';
COMMENT ON COLUMN products.stock IS 'Available stock quantity';

COMMENT ON TABLE users IS 'User accounts reference table';
COMMENT ON COLUMN users.user_id IS 'Unique identifier for the user (UUID)';
COMMENT ON COLUMN users.username IS 'Unique username';
COMMENT ON COLUMN users.email IS 'Unique email address';
