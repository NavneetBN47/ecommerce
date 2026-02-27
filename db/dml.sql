-- SHOPPING CART BACKEND - DATA MANIPULATION LANGUAGE (DML)
-- Seed data and lifecycle operations reflecting LLD business rules
-- User seed data, product catalog, cart lifecycle management

-- SEED DATA FOR USERS
-- Initial user accounts for testing and development
INSERT INTO users (username, password, full_name, email) VALUES 
('mickey', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8QQ67YdSiLuZJ69R6FjQRQ4QjCzPu', 'Mickey Mouse', 'mickey@cartoon.com'),
('minnie', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8QQ67YdSiLuZJ69R6FjQRQ4QjCzPu', 'Minnie Mouse', 'minnie@cartoon.com'),
('donald', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8QQ67YdSiLuZJ69R6FjQRQ4QjCzPu', 'Donald Duck', 'donald@cartoon.com'),
('goofy', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8QQ67YdSiLuZJ69R6FjQRQ4QjCzPu', 'Goofy', 'goofy@cartoon.com'),
('pluto', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8QQ67YdSiLuZJ69R6FjQRQ4QjCzPu', 'Pluto', 'pluto@cartoon.com')
ON CONFLICT (username) DO NOTHING;

-- SEED DATA FOR PRODUCTS
-- Product catalog with various categories and price points
INSERT INTO products (product_name, description, price, available_qty) VALUES 
('Laptop', 'High performance laptop for work and gaming', 1200.00, 10),
('Smartphone', 'Latest model smartphone with advanced features', 800.00, 25),
('Headphones', 'Wireless noise-canceling headphones', 150.00, 50),
('Tablet', '10-inch tablet perfect for reading and media', 400.00, 15),
('Smart Watch', 'Fitness tracking smartwatch with GPS', 250.00, 30),
('Gaming Mouse', 'High-precision gaming mouse with RGB lighting', 75.00, 40),
('Keyboard', 'Mechanical keyboard with backlit keys', 120.00, 35),
('Monitor', '27-inch 4K monitor for professional use', 350.00, 20),
('Webcam', 'HD webcam for video conferencing', 90.00, 45),
('Speakers', 'Bluetooth speakers with premium sound quality', 180.00, 25),
('External HDD', '2TB external hard drive for backup storage', 100.00, 60),
('USB Hub', '7-port USB 3.0 hub with fast charging', 45.00, 80),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 65.00, 55),
('Phone Case', 'Protective case for latest smartphone models', 25.00, 100),
('Screen Protector', 'Tempered glass screen protector', 15.00, 150)
ON CONFLICT DO NOTHING;

-- CART LIFECYCLE OPERATIONS
-- These represent the business logic for cart management as per LLD

-- Function to create cart for user (lazy creation)
-- Called when user adds first item to cart
CREATE OR REPLACE FUNCTION create_user_cart(p_user_id INT)
RETURNS INT AS $$
DECLARE
    v_cart_id INT;
BEGIN
    -- Check if cart already exists
    SELECT cart_id INTO v_cart_id FROM cart WHERE user_id = p_user_id;
    
    IF v_cart_id IS NULL THEN
        -- Create new cart
        INSERT INTO cart (user_id) VALUES (p_user_id) RETURNING cart_id INTO v_cart_id;
    END IF;
    
    RETURN v_cart_id;
END;
$$ LANGUAGE plpgsql;

-- Function to add/update cart item
-- Handles quantity updates and new item additions
CREATE OR REPLACE FUNCTION upsert_cart_item(p_user_id INT, p_product_id INT, p_quantity INT)
RETURNS VOID AS $$
DECLARE
    v_cart_id INT;
BEGIN
    -- Get or create cart
    v_cart_id := create_user_cart(p_user_id);
    
    -- Upsert cart item
    INSERT INTO cart_items (cart_id, product_id, quantity)
    VALUES (v_cart_id, p_product_id, p_quantity)
    ON CONFLICT (cart_id, product_id)
    DO UPDATE SET quantity = EXCLUDED.quantity, added_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Function to remove cart item
-- Removes item and deletes empty cart as per business rules
CREATE OR REPLACE FUNCTION remove_cart_item(p_user_id INT, p_product_id INT)
RETURNS VOID AS $$
DECLARE
    v_cart_id INT;
    v_item_count INT;
BEGIN
    -- Get cart ID
    SELECT cart_id INTO v_cart_id FROM cart WHERE user_id = p_user_id;
    
    IF v_cart_id IS NOT NULL THEN
        -- Remove the item
        DELETE FROM cart_items WHERE cart_id = v_cart_id AND product_id = p_product_id;
        
        -- Check if cart is now empty
        SELECT COUNT(*) INTO v_item_count FROM cart_items WHERE cart_id = v_cart_id;
        
        -- Auto-delete empty cart as per LLD requirement
        IF v_item_count = 0 THEN
            DELETE FROM cart WHERE cart_id = v_cart_id;
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to clear user cart (called on logout)
-- Implements cart cleanup on logout as per LLD
CREATE OR REPLACE FUNCTION clear_user_cart(p_user_id INT)
RETURNS VOID AS $$
BEGIN
    -- Delete cart (cascade will remove cart items)
    DELETE FROM cart WHERE user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;
