-- DML Script for Shopping Cart System (SCRUM-96)
-- Data manipulation operations for UUID-based schema

-- Seed data for users (these users won't get carts as per LLD rules)
INSERT INTO users_v2 (id, username, password, fullName, email, createdDate) 
VALUES 
    (uuid_generate_v4(), 'admin', '$2b$10$hashedpassword1', 'System Administrator', 'admin@example.com', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'testuser1', '$2b$10$hashedpassword2', 'Test User One', 'test1@example.com', CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'testuser2', '$2b$10$hashedpassword3', 'Test User Two', 'test2@example.com', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Seed data for products
INSERT INTO products_v2 (id, name, description, price, availableQuantity) 
VALUES 
    (uuid_generate_v4(), 'Laptop', 'High-performance laptop for professionals', 1299.99, 50),
    (uuid_generate_v4(), 'Wireless Mouse', 'Ergonomic wireless mouse with long battery life', 29.99, 200),
    (uuid_generate_v4(), 'Mechanical Keyboard', 'RGB mechanical keyboard for gaming and typing', 149.99, 75),
    (uuid_generate_v4(), 'Monitor', '27-inch 4K monitor with USB-C connectivity', 399.99, 30),
    (uuid_generate_v4(), 'Webcam', 'HD webcam for video conferencing', 79.99, 100),
    (uuid_generate_v4(), 'Headphones', 'Noise-cancelling wireless headphones', 199.99, 60),
    (uuid_generate_v4(), 'USB Cable', 'USB-C to USB-A cable 6ft', 12.99, 500),
    (uuid_generate_v4(), 'Phone Case', 'Protective case for smartphones', 24.99, 150),
    (uuid_generate_v4(), 'Tablet', '10-inch tablet with stylus support', 299.99, 40),
    (uuid_generate_v4(), 'Power Bank', 'Portable charger 20000mAh capacity', 49.99, 80)
ON CONFLICT DO NOTHING;

-- User registration procedure
CREATE OR REPLACE FUNCTION register_user(
    p_username VARCHAR(255),
    p_password VARCHAR(255),
    p_fullName VARCHAR(255),
    p_email VARCHAR(255)
) RETURNS UUID AS $$
DECLARE
    new_user_id UUID;
BEGIN
    INSERT INTO users_v2 (username, password, fullName, email)
    VALUES (p_username, p_password, p_fullName, p_email)
    RETURNING id INTO new_user_id;
    
    RETURN new_user_id;
EXCEPTION
    WHEN unique_violation THEN
        RAISE EXCEPTION 'Username already exists: %', p_username;
END;
$$ LANGUAGE plpgsql;

-- Create or get user cart
CREATE OR REPLACE FUNCTION get_or_create_cart(p_user_id UUID) 
RETURNS UUID AS $$
DECLARE
    cart_id UUID;
BEGIN
    -- Try to get existing cart
    SELECT id INTO cart_id FROM carts_v2 WHERE userId = p_user_id;
    
    -- If no cart exists, create one
    IF cart_id IS NULL THEN
        INSERT INTO carts_v2 (userId) VALUES (p_user_id) RETURNING id INTO cart_id;
    END IF;
    
    RETURN cart_id;
END;
$$ LANGUAGE plpgsql;

-- Add item to cart
CREATE OR REPLACE FUNCTION add_to_cart(
    p_user_id UUID,
    p_product_id UUID,
    p_quantity INTEGER
) RETURNS VOID AS $$
DECLARE
    cart_id UUID;
    available_qty INTEGER;
BEGIN
    -- Validate quantity
    IF p_quantity <= 0 THEN
        RAISE EXCEPTION 'Quantity must be greater than 0';
    END IF;
    
    -- Check product availability
    SELECT availableQuantity INTO available_qty FROM products_v2 WHERE id = p_product_id;
    IF available_qty IS NULL THEN
        RAISE EXCEPTION 'Product not found';
    END IF;
    
    IF available_qty < p_quantity THEN
        RAISE EXCEPTION 'Insufficient inventory. Available: %, Requested: %', available_qty, p_quantity;
    END IF;
    
    -- Get or create cart
    cart_id := get_or_create_cart(p_user_id);
    
    -- Add or update cart item
    INSERT INTO cart_items_v2 (cartId, productId, quantity)
    VALUES (cart_id, p_product_id, p_quantity)
    ON CONFLICT (cartId, productId) 
    DO UPDATE SET quantity = cart_items_v2.quantity + p_quantity;
END;
$$ LANGUAGE plpgsql;

-- Update cart item quantity
CREATE OR REPLACE FUNCTION update_cart_item(
    p_user_id UUID,
    p_product_id UUID,
    p_new_quantity INTEGER
) RETURNS VOID AS $$
DECLARE
    cart_id UUID;
BEGIN
    -- Get user's cart
    SELECT id INTO cart_id FROM carts_v2 WHERE userId = p_user_id;
    
    IF cart_id IS NULL THEN
        RAISE EXCEPTION 'No cart found for user';
    END IF;
    
    IF p_new_quantity <= 0 THEN
        -- Remove item if quantity is 0 or negative
        DELETE FROM cart_items_v2 WHERE cartId = cart_id AND productId = p_product_id;
    ELSE
        -- Update quantity
        UPDATE cart_items_v2 
        SET quantity = p_new_quantity 
        WHERE cartId = cart_id AND productId = p_product_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Remove item from cart
CREATE OR REPLACE FUNCTION remove_from_cart(
    p_user_id UUID,
    p_product_id UUID
) RETURNS VOID AS $$
DECLARE
    cart_id UUID;
BEGIN
    -- Get user's cart
    SELECT id INTO cart_id FROM carts_v2 WHERE userId = p_user_id;
    
    IF cart_id IS NULL THEN
        RAISE EXCEPTION 'No cart found for user';
    END IF;
    
    -- Remove item
    DELETE FROM cart_items_v2 WHERE cartId = cart_id AND productId = p_product_id;
END;
$$ LANGUAGE plpgsql;

-- Clear entire cart
CREATE OR REPLACE FUNCTION clear_cart(p_user_id UUID) 
RETURNS VOID AS $$
DECLARE
    cart_id UUID;
BEGIN
    -- Get user's cart
    SELECT id INTO cart_id FROM carts_v2 WHERE userId = p_user_id;
    
    IF cart_id IS NOT NULL THEN
        -- Delete all cart items (cart will be auto-deleted by trigger)
        DELETE FROM cart_items_v2 WHERE cartId = cart_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Update product inventory
CREATE OR REPLACE FUNCTION update_product_inventory(
    p_product_id UUID,
    p_new_quantity INTEGER
) RETURNS VOID AS $$
BEGIN
    IF p_new_quantity < 0 THEN
        RAISE EXCEPTION 'Inventory quantity cannot be negative';
    END IF;
    
    UPDATE products_v2 
    SET availableQuantity = p_new_quantity 
    WHERE id = p_product_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Product not found';
    END IF;
END;
$$ LANGUAGE plpgsql;
