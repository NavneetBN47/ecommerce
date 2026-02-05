-- Shopping Cart System Database Data Manipulation (DML)
-- Generated from Low-Level Design (LLD) as authoritative source
-- This script contains sample data and data manipulation procedures

USE ecommerce_db;

-- Insert sample categories
INSERT IGNORE INTO categories (name, description, parent_category_id, is_active, sort_order) VALUES
('Electronics', 'Electronic devices and accessories', NULL, TRUE, 1),
('Clothing', 'Apparel and fashion items', NULL, TRUE, 2),
('Books', 'Books and educational materials', NULL, TRUE, 3),
('Home & Garden', 'Home improvement and garden supplies', NULL, TRUE, 4),
('Sports & Outdoors', 'Sports equipment and outdoor gear', NULL, TRUE, 5);

-- Insert subcategories
INSERT IGNORE INTO categories (name, description, parent_category_id, is_active, sort_order) VALUES
('Smartphones', 'Mobile phones and accessories', 1, TRUE, 1),
('Laptops', 'Portable computers', 1, TRUE, 2),
('Tablets', 'Tablet computers', 1, TRUE, 3),
('Men''s Clothing', 'Clothing for men', 2, TRUE, 1),
('Women''s Clothing', 'Clothing for women', 2, TRUE, 2),
('Fiction', 'Fiction books', 3, TRUE, 1),
('Non-Fiction', 'Non-fiction books', 3, TRUE, 2);

-- Insert sample users
INSERT IGNORE INTO users (username, email, password_hash, first_name, last_name, phone, is_active) VALUES
('john_doe', 'john.doe@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', '+1234567890', TRUE),
('jane_smith', 'jane.smith@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Smith', '+1234567891', TRUE),
('bob_wilson', 'bob.wilson@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bob', 'Wilson', '+1234567892', TRUE),
('alice_brown', 'alice.brown@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Alice', 'Brown', '+1234567893', TRUE),
('charlie_davis', 'charlie.davis@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Charlie', 'Davis', '+1234567894', TRUE);

-- Insert sample user addresses
INSERT IGNORE INTO user_addresses (user_id, address_type, street_address, city, state, postal_code, country, is_default) VALUES
(1, 'both', '123 Main St', 'New York', 'NY', '10001', 'USA', TRUE),
(1, 'shipping', '456 Oak Ave', 'New York', 'NY', '10002', 'USA', FALSE),
(2, 'both', '789 Pine St', 'Los Angeles', 'CA', '90001', 'USA', TRUE),
(3, 'both', '321 Elm St', 'Chicago', 'IL', '60601', 'USA', TRUE),
(4, 'both', '654 Maple Ave', 'Houston', 'TX', '77001', 'USA', TRUE),
(5, 'both', '987 Cedar St', 'Phoenix', 'AZ', '85001', 'USA', TRUE);

-- Insert sample products
INSERT IGNORE INTO products (name, description, sku, category_id, brand, price, cost_price, weight, color, is_active, is_featured) VALUES
('iPhone 14 Pro', 'Latest Apple smartphone with advanced camera system', 'IPH14PRO-128-SG', 6, 'Apple', 999.99, 750.00, 0.206, 'Space Gray', TRUE, TRUE),
('Samsung Galaxy S23', 'Premium Android smartphone with excellent display', 'SGS23-256-BLK', 6, 'Samsung', 899.99, 650.00, 0.168, 'Black', TRUE, TRUE),
('MacBook Pro 14"', 'Professional laptop with M2 chip', 'MBP14-M2-512', 7, 'Apple', 1999.99, 1500.00, 1.6, 'Silver', TRUE, TRUE),
('Dell XPS 13', 'Ultra-portable laptop for professionals', 'DXPS13-I7-512', 7, 'Dell', 1299.99, 950.00, 1.2, 'Platinum Silver', TRUE, FALSE),
('iPad Air', 'Versatile tablet for work and entertainment', 'IPADAIR-64-BLU', 8, 'Apple', 599.99, 450.00, 0.461, 'Sky Blue', TRUE, TRUE),
('Men''s Cotton T-Shirt', 'Comfortable cotton t-shirt for everyday wear', 'MTSHIRT-L-BLU', 9, 'Generic', 19.99, 8.00, 0.2, 'Blue', TRUE, FALSE),
('Women''s Denim Jeans', 'Classic fit denim jeans', 'WJEANS-32-IND', 10, 'Levi''s', 79.99, 35.00, 0.6, 'Indigo', TRUE, FALSE),
('The Great Gatsby', 'Classic American novel by F. Scott Fitzgerald', 'BOOK-GG-PB', 11, 'Scribner', 12.99, 5.00, 0.3, 'N/A', TRUE, FALSE),
('Atomic Habits', 'Self-help book about building good habits', 'BOOK-AH-HC', 12, 'Avery', 24.99, 12.00, 0.5, 'N/A', TRUE, TRUE);

-- Insert product images
INSERT IGNORE INTO product_images (product_id, image_url, alt_text, is_primary, sort_order) VALUES
(1, '/images/products/iphone14pro-1.jpg', 'iPhone 14 Pro front view', TRUE, 1),
(1, '/images/products/iphone14pro-2.jpg', 'iPhone 14 Pro back view', FALSE, 2),
(2, '/images/products/galaxys23-1.jpg', 'Samsung Galaxy S23 front view', TRUE, 1),
(2, '/images/products/galaxys23-2.jpg', 'Samsung Galaxy S23 back view', FALSE, 2),
(3, '/images/products/macbookpro14-1.jpg', 'MacBook Pro 14 inch open', TRUE, 1),
(4, '/images/products/dellxps13-1.jpg', 'Dell XPS 13 laptop', TRUE, 1),
(5, '/images/products/ipadair-1.jpg', 'iPad Air front view', TRUE, 1),
(6, '/images/products/tshirt-blue-1.jpg', 'Blue cotton t-shirt', TRUE, 1),
(7, '/images/products/jeans-indigo-1.jpg', 'Indigo denim jeans', TRUE, 1),
(8, '/images/products/great-gatsby-1.jpg', 'The Great Gatsby book cover', TRUE, 1),
(9, '/images/products/atomic-habits-1.jpg', 'Atomic Habits book cover', TRUE, 1);

-- Insert inventory data
INSERT IGNORE INTO inventory (product_id, quantity_available, quantity_reserved, reorder_level, max_stock_level, warehouse_location) VALUES
(1, 50, 5, 10, 100, 'Warehouse A'),
(2, 75, 3, 15, 150, 'Warehouse A'),
(3, 25, 2, 5, 50, 'Warehouse B'),
(4, 40, 1, 8, 80, 'Warehouse B'),
(5, 60, 4, 12, 120, 'Warehouse A'),
(6, 200, 10, 50, 500, 'Warehouse C'),
(7, 150, 8, 30, 300, 'Warehouse C'),
(8, 100, 0, 20, 200, 'Warehouse D'),
(9, 80, 2, 15, 150, 'Warehouse D');

-- Insert sample coupons
INSERT IGNORE INTO coupons (code, name, description, discount_type, discount_value, minimum_order_amount, usage_limit, is_active, valid_from, valid_until) VALUES
('WELCOME10', 'Welcome Discount', '10% off for new customers', 'percentage', 10.00, 50.00, 1000, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
('SAVE20', 'Save $20', '$20 off orders over $100', 'fixed_amount', 20.00, 100.00, 500, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY)),
('ELECTRONICS15', 'Electronics Sale', '15% off electronics', 'percentage', 15.00, 200.00, 200, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY)),
('FREESHIP', 'Free Shipping', 'Free shipping on orders over $75', 'fixed_amount', 10.00, 75.00, NULL, TRUE, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY));

-- Create stored procedures for common operations

DELIMITER //

-- Procedure to add item to cart
CREATE PROCEDURE IF NOT EXISTS AddToCart(
    IN p_user_id INT,
    IN p_session_id VARCHAR(255),
    IN p_product_id INT,
    IN p_quantity INT
)
BEGIN
    DECLARE v_cart_id INT;
    DECLARE v_unit_price DECIMAL(10,2);
    DECLARE v_existing_quantity INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Get product price
    SELECT price INTO v_unit_price FROM products WHERE product_id = p_product_id AND is_active = TRUE;
    
    IF v_unit_price IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Product not found or inactive';
    END IF;

    -- Find or create cart
    IF p_user_id IS NOT NULL THEN
        SELECT cart_id INTO v_cart_id FROM shopping_carts 
        WHERE user_id = p_user_id AND status = 'active' 
        ORDER BY created_at DESC LIMIT 1;
    ELSE
        SELECT cart_id INTO v_cart_id FROM shopping_carts 
        WHERE session_id = p_session_id AND status = 'active' 
        ORDER BY created_at DESC LIMIT 1;
    END IF;

    IF v_cart_id IS NULL THEN
        INSERT INTO shopping_carts (user_id, session_id, status) 
        VALUES (p_user_id, p_session_id, 'active');
        SET v_cart_id = LAST_INSERT_ID();
    END IF;

    -- Check if item already exists in cart
    SELECT quantity INTO v_existing_quantity 
    FROM cart_items 
    WHERE cart_id = v_cart_id AND product_id = p_product_id;

    IF v_existing_quantity > 0 THEN
        -- Update existing item
        UPDATE cart_items 
        SET quantity = quantity + p_quantity, 
            unit_price = v_unit_price,
            updated_at = NOW()
        WHERE cart_id = v_cart_id AND product_id = p_product_id;
    ELSE
        -- Insert new item
        INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
        VALUES (v_cart_id, p_product_id, p_quantity, v_unit_price);
    END IF;

    -- Update cart totals
    CALL UpdateCartTotals(v_cart_id);

    COMMIT;
END//

-- Procedure to update cart totals
CREATE PROCEDURE IF NOT EXISTS UpdateCartTotals(
    IN p_cart_id INT
)
BEGIN
    DECLARE v_total_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_total_items INT DEFAULT 0;

    SELECT 
        COALESCE(SUM(total_price), 0.00),
        COALESCE(SUM(quantity), 0)
    INTO v_total_amount, v_total_items
    FROM cart_items 
    WHERE cart_id = p_cart_id;

    UPDATE shopping_carts 
    SET total_amount = v_total_amount,
        total_items = v_total_items,
        updated_at = NOW()
    WHERE cart_id = p_cart_id;
END//

-- Procedure to remove item from cart
CREATE PROCEDURE IF NOT EXISTS RemoveFromCart(
    IN p_cart_id INT,
    IN p_product_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    DELETE FROM cart_items 
    WHERE cart_id = p_cart_id AND product_id = p_product_id;

    CALL UpdateCartTotals(p_cart_id);

    COMMIT;
END//

-- Procedure to create order from cart
CREATE PROCEDURE IF NOT EXISTS CreateOrderFromCart(
    IN p_cart_id INT,
    IN p_shipping_address_id INT,
    IN p_billing_address_id INT,
    IN p_payment_method VARCHAR(50),
    OUT p_order_id INT
)
BEGIN
    DECLARE v_user_id INT;
    DECLARE v_subtotal DECIMAL(10,2);
    DECLARE v_tax_amount DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_shipping_amount DECIMAL(10,2) DEFAULT 10.00;
    DECLARE v_total_amount DECIMAL(10,2);
    DECLARE v_order_number VARCHAR(50);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Get cart details
    SELECT user_id, total_amount INTO v_user_id, v_subtotal
    FROM shopping_carts 
    WHERE cart_id = p_cart_id AND status = 'active';

    IF v_user_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cart not found or inactive';
    END IF;

    -- Calculate tax (8% for example)
    SET v_tax_amount = v_subtotal * 0.08;
    
    -- Free shipping for orders over $75
    IF v_subtotal >= 75.00 THEN
        SET v_shipping_amount = 0.00;
    END IF;

    SET v_total_amount = v_subtotal + v_tax_amount + v_shipping_amount;

    -- Generate order number
    SET v_order_number = CONCAT('ORD-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND() * 10000), 4, '0'));

    -- Create order
    INSERT INTO orders (
        user_id, order_number, status, subtotal, tax_amount, 
        shipping_amount, total_amount, payment_method,
        shipping_address_id, billing_address_id
    ) VALUES (
        v_user_id, v_order_number, 'pending', v_subtotal, v_tax_amount,
        v_shipping_amount, v_total_amount, p_payment_method,
        p_shipping_address_id, p_billing_address_id
    );

    SET p_order_id = LAST_INSERT_ID();

    -- Copy cart items to order items
    INSERT INTO order_items (order_id, product_id, quantity, unit_price)
    SELECT p_order_id, product_id, quantity, unit_price
    FROM cart_items
    WHERE cart_id = p_cart_id;

    -- Update inventory (reserve quantities)
    UPDATE inventory i
    JOIN cart_items ci ON i.product_id = ci.product_id
    SET i.quantity_reserved = i.quantity_reserved + ci.quantity
    WHERE ci.cart_id = p_cart_id;

    -- Mark cart as converted
    UPDATE shopping_carts 
    SET status = 'converted', updated_at = NOW()
    WHERE cart_id = p_cart_id;

    COMMIT;
END//

-- Procedure to apply coupon
CREATE PROCEDURE IF NOT EXISTS ApplyCoupon(
    IN p_cart_id INT,
    IN p_coupon_code VARCHAR(50),
    OUT p_discount_amount DECIMAL(10,2),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_coupon_id INT;
    DECLARE v_discount_type ENUM('percentage', 'fixed_amount');
    DECLARE v_discount_value DECIMAL(10,2);
    DECLARE v_minimum_order_amount DECIMAL(10,2);
    DECLARE v_maximum_discount_amount DECIMAL(10,2);
    DECLARE v_usage_limit INT;
    DECLARE v_usage_count INT;
    DECLARE v_is_active BOOLEAN;
    DECLARE v_valid_until TIMESTAMP;
    DECLARE v_cart_total DECIMAL(10,2);
    
    SET p_discount_amount = 0.00;
    SET p_message = 'Invalid coupon';

    -- Get coupon details
    SELECT 
        coupon_id, discount_type, discount_value, minimum_order_amount,
        maximum_discount_amount, usage_limit, usage_count, is_active, valid_until
    INTO 
        v_coupon_id, v_discount_type, v_discount_value, v_minimum_order_amount,
        v_maximum_discount_amount, v_usage_limit, v_usage_count, v_is_active, v_valid_until
    FROM coupons 
    WHERE code = p_coupon_code;

    IF v_coupon_id IS NULL THEN
        SET p_message = 'Coupon not found';
    ELSEIF NOT v_is_active THEN
        SET p_message = 'Coupon is not active';
    ELSEIF v_valid_until IS NOT NULL AND NOW() > v_valid_until THEN
        SET p_message = 'Coupon has expired';
    ELSEIF v_usage_limit IS NOT NULL AND v_usage_count >= v_usage_limit THEN
        SET p_message = 'Coupon usage limit exceeded';
    ELSE
        -- Get cart total
        SELECT total_amount INTO v_cart_total
        FROM shopping_carts
        WHERE cart_id = p_cart_id;

        IF v_cart_total < v_minimum_order_amount THEN
            SET p_message = CONCAT('Minimum order amount is $', v_minimum_order_amount);
        ELSE
            -- Calculate discount
            IF v_discount_type = 'percentage' THEN
                SET p_discount_amount = v_cart_total * (v_discount_value / 100);
            ELSE
                SET p_discount_amount = v_discount_value;
            END IF;

            -- Apply maximum discount limit
            IF v_maximum_discount_amount IS NOT NULL AND p_discount_amount > v_maximum_discount_amount THEN
                SET p_discount_amount = v_maximum_discount_amount;
            END IF;

            -- Ensure discount doesn't exceed cart total
            IF p_discount_amount > v_cart_total THEN
                SET p_discount_amount = v_cart_total;
            END IF;

            SET p_message = 'Coupon applied successfully';
        END IF;
    END IF;
END//

DELIMITER ;

-- Insert sample shopping carts
INSERT IGNORE INTO shopping_carts (user_id, status, total_amount, total_items) VALUES
(1, 'active', 0.00, 0),
(2, 'active', 0.00, 0),
(3, 'active', 0.00, 0);

-- Add some items to carts using the stored procedure
CALL AddToCart(1, NULL, 1, 1); -- iPhone for user 1
CALL AddToCart(1, NULL, 6, 2); -- T-shirts for user 1
CALL AddToCart(2, NULL, 3, 1); -- MacBook for user 2
CALL AddToCart(3, NULL, 8, 1); -- Book for user 3
CALL AddToCart(3, NULL, 9, 1); -- Another book for user 3

-- Insert sample reviews
INSERT IGNORE INTO reviews (product_id, user_id, rating, title, comment, is_verified_purchase, is_approved) VALUES
(1, 2, 5, 'Excellent phone!', 'The iPhone 14 Pro is amazing. Great camera and performance.', TRUE, TRUE),
(1, 3, 4, 'Good but expensive', 'Great phone but quite pricey. Worth it for the features.', TRUE, TRUE),
(3, 1, 5, 'Perfect for work', 'The MacBook Pro is incredibly fast and reliable for development work.', TRUE, TRUE),
(6, 4, 4, 'Comfortable fit', 'Nice quality t-shirt, fits well and comfortable to wear.', TRUE, TRUE),
(8, 5, 5, 'Classic literature', 'One of the best American novels ever written. Highly recommended.', TRUE, TRUE);

-- Insert sample wishlists
INSERT IGNORE INTO wishlists (user_id, product_id) VALUES
(1, 3), -- User 1 wants MacBook
(1, 5), -- User 1 wants iPad
(2, 2), -- User 2 wants Galaxy S23
(3, 1), -- User 3 wants iPhone
(4, 7), -- User 4 wants jeans
(5, 9); -- User 5 wants Atomic Habits book

-- Create indexes for better performance on frequently queried columns
CREATE INDEX IF NOT EXISTS idx_products_category_active ON products(category_id, is_active);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_product ON cart_items(cart_id, product_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);
CREATE INDEX IF NOT EXISTS idx_reviews_product_approved ON reviews(product_id, is_approved);
CREATE INDEX IF NOT EXISTS idx_inventory_product_available ON inventory(product_id, quantity_available);

-- Data manipulation setup complete
-- Sample data inserted for all major entities
-- Stored procedures created for common operations
-- Performance indexes added