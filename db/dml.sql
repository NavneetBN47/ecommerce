-- DML SCRIPT FOR SHOPPING CART SYSTEM
-- Contains seed data and application-level DML operations
-- Generated from Low Level Design (LLD) - Additive and Non-Destructive

-- Seed data for users (using INSERT ... ON CONFLICT to avoid duplicates)
INSERT INTO users (username, password, full_name, email, roles) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye7VfPiYxlGcHcz8.SkVhmPpNY7ovOaWa', 'System Administrator', 'admin@shoppingcart.com', 'ADMIN'),
('john_doe', '$2a$10$N9qo8uLOickgx2ZMRZoMye7VfPiYxlGcHcz8.SkVhmPpNY7ovOaWa', 'John Doe', 'john.doe@email.com', 'USER'),
('jane_smith', '$2a$10$N9qo8uLOickgx2ZMRZoMye7VfPiYxlGcHcz8.SkVhmPpNY7ovOaWa', 'Jane Smith', 'jane.smith@email.com', 'USER'),
('bob_wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMye7VfPiYxlGcHcz8.SkVhmPpNY7ovOaWa', 'Bob Wilson', 'bob.wilson@email.com', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Seed data for products (using INSERT ... ON CONFLICT to avoid duplicates)
INSERT INTO products (product_name, description, price, available_qty, category) VALUES
('Laptop Pro 15', 'High-performance laptop with 16GB RAM and 512GB SSD', 1299.99, 50, 'Electronics'),
('Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 29.99, 200, 'Electronics'),
('Coffee Mug', 'Ceramic coffee mug with company logo', 12.99, 100, 'Office Supplies'),
('Notebook Set', 'Set of 3 premium notebooks for note-taking', 24.99, 75, 'Office Supplies'),
('Smartphone X1', 'Latest smartphone with advanced camera features', 899.99, 30, 'Electronics'),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 45.99, 60, 'Office Supplies'),
('Bluetooth Headphones', 'Noise-cancelling wireless headphones', 199.99, 40, 'Electronics'),
('Water Bottle', 'Insulated stainless steel water bottle', 19.99, 150, 'Lifestyle'),
('Backpack', 'Durable laptop backpack with multiple compartments', 79.99, 80, 'Lifestyle'),
('Desk Organizer', 'Wooden desk organizer with multiple slots', 34.99, 90, 'Office Supplies')
ON CONFLICT (product_name) DO NOTHING;

-- Sample DML operations for application use

-- Function to add item to cart (respects business rules)
CREATE OR REPLACE FUNCTION add_to_cart(
    p_user_id INT,
    p_product_id INT,
    p_quantity INT
) RETURNS BOOLEAN AS $$
DECLARE
    v_cart_id INT;
    v_current_quantity INT := 0;
    v_product_price DECIMAL(10,2);
    v_available_qty INT;
BEGIN
    -- Check if product exists and has sufficient stock
    SELECT price, available_qty INTO v_product_price, v_available_qty
    FROM products WHERE product_id = p_product_id;
    
    IF NOT FOUND OR v_available_qty < p_quantity THEN
        RETURN FALSE;
    END IF;
    
    -- Get or create active cart for user
    SELECT cart_id INTO v_cart_id
    FROM cart WHERE user_id = p_user_id AND status = 'ACTIVE';
    
    IF NOT FOUND THEN
        INSERT INTO cart (user_id, status) VALUES (p_user_id, 'ACTIVE')
        RETURNING cart_id INTO v_cart_id;
    END IF;
    
    -- Check current quantity in cart
    SELECT COALESCE(quantity, 0) INTO v_current_quantity
    FROM cart_items WHERE cart_id = v_cart_id AND product_id = p_product_id;
    
    -- Check if adding quantity would exceed limit
    IF v_current_quantity + p_quantity > 10 THEN
        RETURN FALSE;
    END IF;
    
    -- Add or update cart item
    INSERT INTO cart_items (cart_id, product_id, quantity, price_at_addition)
    VALUES (v_cart_id, p_product_id, p_quantity, v_product_price)
    ON CONFLICT (cart_id, product_id) DO UPDATE SET
        quantity = cart_items.quantity + p_quantity,
        updated_at = CURRENT_TIMESTAMP;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function to checkout cart (atomically decrements stock)
CREATE OR REPLACE FUNCTION checkout_cart(p_user_id INT) RETURNS BOOLEAN AS $$
DECLARE
    v_cart_id INT;
    cart_item RECORD;
BEGIN
    -- Get active cart
    SELECT cart_id INTO v_cart_id
    FROM cart WHERE user_id = p_user_id AND status = 'ACTIVE';
    
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;
    
    -- Check stock availability for all items
    FOR cart_item IN
        SELECT ci.product_id, ci.quantity, p.available_qty
        FROM cart_items ci
        JOIN products p ON ci.product_id = p.product_id
        WHERE ci.cart_id = v_cart_id
    LOOP
        IF cart_item.available_qty < cart_item.quantity THEN
            RETURN FALSE;
        END IF;
    END LOOP;
    
    -- Decrement stock for all items
    UPDATE products SET
        available_qty = available_qty - ci.quantity,
        updated_at = CURRENT_TIMESTAMP
    FROM cart_items ci
    WHERE products.product_id = ci.product_id
    AND ci.cart_id = v_cart_id;
    
    -- Mark cart as checked out
    UPDATE cart SET
        status = 'CHECKED_OUT',
        updated_at = CURRENT_TIMESTAMP
    WHERE cart_id = v_cart_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function to clear active cart
CREATE OR REPLACE FUNCTION clear_cart(p_user_id INT) RETURNS BOOLEAN AS $$
DECLARE
    v_cart_id INT;
BEGIN
    SELECT cart_id INTO v_cart_id
    FROM cart WHERE user_id = p_user_id AND status = 'ACTIVE';
    
    IF FOUND THEN
        -- Remove all items from cart (non-destructive, just removes cart items)
        UPDATE cart_items SET quantity = 0, updated_at = CURRENT_TIMESTAMP
        WHERE cart_id = v_cart_id;
        RETURN TRUE;
    END IF;
    
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;

-- Update existing data to match LLD requirements (additive only)
UPDATE users SET roles = 'USER' WHERE roles IS NULL;
UPDATE cart SET status = 'ACTIVE' WHERE status IS NULL;

-- Sample data insertion for testing
INSERT INTO cart (user_id, status) 
SELECT user_id, 'ACTIVE' FROM users WHERE username IN ('john_doe', 'jane_smith')
ON CONFLICT (user_id) DO NOTHING;

-- Add sample cart items for testing
INSERT INTO cart_items (cart_id, product_id, quantity, price_at_addition)
SELECT c.cart_id, p.product_id, 2, p.price
FROM cart c, products p, users u
WHERE c.user_id = u.user_id 
AND u.username = 'john_doe'
AND p.product_name IN ('Laptop Pro 15', 'Wireless Mouse')
AND c.status = 'ACTIVE'
ON CONFLICT DO NOTHING;