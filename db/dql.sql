-- DQL Script for Shopping Cart System (SCRUM-96)
-- Query operations for UUID-based schema

-- User authentication and profile queries

-- Get user by username (for login)
CREATE OR REPLACE FUNCTION get_user_by_username(p_username VARCHAR(255))
RETURNS TABLE(
    id UUID,
    username VARCHAR(255),
    password VARCHAR(255),
    fullName VARCHAR(255),
    email VARCHAR(255),
    createdDate TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT u.id, u.username, u.password, u.fullName, u.email, u.createdDate
    FROM users_v2 u
    WHERE u.username = p_username;
END;
$$ LANGUAGE plpgsql;

-- Get user profile by ID
CREATE OR REPLACE FUNCTION get_user_profile(p_user_id UUID)
RETURNS TABLE(
    id UUID,
    username VARCHAR(255),
    fullName VARCHAR(255),
    email VARCHAR(255),
    createdDate TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT u.id, u.username, u.fullName, u.email, u.createdDate
    FROM users_v2 u
    WHERE u.id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- Product catalog queries

-- Get all products with availability
CREATE OR REPLACE FUNCTION get_all_products()
RETURNS TABLE(
    id UUID,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2),
    availableQuantity INTEGER,
    inStock BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.name,
        p.description,
        p.price,
        p.availableQuantity,
        (p.availableQuantity > 0) as inStock
    FROM products_v2 p
    ORDER BY p.name;
END;
$$ LANGUAGE plpgsql;

-- Get product by ID
CREATE OR REPLACE FUNCTION get_product_by_id(p_product_id UUID)
RETURNS TABLE(
    id UUID,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2),
    availableQuantity INTEGER,
    inStock BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.name,
        p.description,
        p.price,
        p.availableQuantity,
        (p.availableQuantity > 0) as inStock
    FROM products_v2 p
    WHERE p.id = p_product_id;
END;
$$ LANGUAGE plpgsql;

-- Search products by name
CREATE OR REPLACE FUNCTION search_products(p_search_term VARCHAR(255))
RETURNS TABLE(
    id UUID,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2),
    availableQuantity INTEGER,
    inStock BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.name,
        p.description,
        p.price,
        p.availableQuantity,
        (p.availableQuantity > 0) as inStock
    FROM products_v2 p
    WHERE p.name ILIKE '%' || p_search_term || '%'
       OR p.description ILIKE '%' || p_search_term || '%'
    ORDER BY p.name;
END;
$$ LANGUAGE plpgsql;

-- Cart and cart item queries

-- Get user's cart with items
CREATE OR REPLACE FUNCTION get_user_cart(p_user_id UUID)
RETURNS TABLE(
    cart_id UUID,
    cart_created_date TIMESTAMP,
    item_id UUID,
    product_id UUID,
    product_name VARCHAR(255),
    product_price DECIMAL(10,2),
    quantity INTEGER,
    item_total DECIMAL(10,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id as cart_id,
        c.createdDate as cart_created_date,
        ci.id as item_id,
        p.id as product_id,
        p.name as product_name,
        p.price as product_price,
        ci.quantity,
        (p.price * ci.quantity) as item_total
    FROM carts_v2 c
    JOIN cart_items_v2 ci ON c.id = ci.cartId
    JOIN products_v2 p ON ci.productId = p.id
    WHERE c.userId = p_user_id
    ORDER BY ci.id;
END;
$$ LANGUAGE plpgsql;

-- Get cart summary (totals)
CREATE OR REPLACE FUNCTION get_cart_summary(p_user_id UUID)
RETURNS TABLE(
    cart_id UUID,
    total_items INTEGER,
    total_amount DECIMAL(10,2),
    created_date TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id as cart_id,
        CAST(SUM(ci.quantity) AS INTEGER) as total_items,
        SUM(p.price * ci.quantity) as total_amount,
        c.createdDate as created_date
    FROM carts_v2 c
    JOIN cart_items_v2 ci ON c.id = ci.cartId
    JOIN products_v2 p ON ci.productId = p.id
    WHERE c.userId = p_user_id
    GROUP BY c.id, c.createdDate;
END;
$$ LANGUAGE plpgsql;

-- Check if user has a cart
CREATE OR REPLACE FUNCTION user_has_cart(p_user_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    cart_exists BOOLEAN;
BEGIN
    SELECT EXISTS(
        SELECT 1 FROM carts_v2 WHERE userId = p_user_id
    ) INTO cart_exists;
    
    RETURN cart_exists;
END;
$$ LANGUAGE plpgsql;

-- Get cart item count for user
CREATE OR REPLACE FUNCTION get_cart_item_count(p_user_id UUID)
RETURNS INTEGER AS $$
DECLARE
    item_count INTEGER;
BEGIN
    SELECT COALESCE(SUM(ci.quantity), 0)
    INTO item_count
    FROM carts_v2 c
    JOIN cart_items_v2 ci ON c.id = ci.cartId
    WHERE c.userId = p_user_id;
    
    RETURN item_count;
END;
$$ LANGUAGE plpgsql;

-- Administrative queries

-- Get all users (admin function)
CREATE OR REPLACE FUNCTION get_all_users()
RETURNS TABLE(
    id UUID,
    username VARCHAR(255),
    fullName VARCHAR(255),
    email VARCHAR(255),
    createdDate TIMESTAMP,
    has_cart BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.id,
        u.username,
        u.fullName,
        u.email,
        u.createdDate,
        EXISTS(SELECT 1 FROM carts_v2 c WHERE c.userId = u.id) as has_cart
    FROM users_v2 u
    ORDER BY u.createdDate DESC;
END;
$$ LANGUAGE plpgsql;

-- Get products with low inventory
CREATE OR REPLACE FUNCTION get_low_inventory_products(p_threshold INTEGER DEFAULT 10)
RETURNS TABLE(
    id UUID,
    name VARCHAR(255),
    availableQuantity INTEGER,
    price DECIMAL(10,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT p.id, p.name, p.availableQuantity, p.price
    FROM products_v2 p
    WHERE p.availableQuantity <= p_threshold
    ORDER BY p.availableQuantity ASC;
END;
$$ LANGUAGE plpgsql;

-- Get cart statistics
CREATE OR REPLACE FUNCTION get_cart_statistics()
RETURNS TABLE(
    total_carts INTEGER,
    total_cart_items INTEGER,
    average_items_per_cart DECIMAL(10,2),
    total_cart_value DECIMAL(10,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(DISTINCT c.id)::INTEGER as total_carts,
        COUNT(ci.id)::INTEGER as total_cart_items,
        ROUND(AVG(items_per_cart.item_count), 2) as average_items_per_cart,
        SUM(p.price * ci.quantity) as total_cart_value
    FROM carts_v2 c
    LEFT JOIN cart_items_v2 ci ON c.id = ci.cartId
    LEFT JOIN products_v2 p ON ci.productId = p.id
    LEFT JOIN (
        SELECT cartId, SUM(quantity) as item_count
        FROM cart_items_v2
        GROUP BY cartId
    ) items_per_cart ON c.id = items_per_cart.cartId;
END;
$$ LANGUAGE plpgsql;

-- Validation queries

-- Check for orphaned carts (should not exist due to constraints)
CREATE OR REPLACE FUNCTION find_orphaned_carts()
RETURNS TABLE(
    cart_id UUID,
    user_id UUID
) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id as cart_id, c.userId as user_id
    FROM carts_v2 c
    LEFT JOIN users_v2 u ON c.userId = u.id
    WHERE u.id IS NULL;
END;
$$ LANGUAGE plpgsql;

-- Check for empty carts (should not exist due to business rules)
CREATE OR REPLACE FUNCTION find_empty_carts()
RETURNS TABLE(
    cart_id UUID,
    user_id UUID,
    created_date TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id as cart_id, c.userId as user_id, c.createdDate as created_date
    FROM carts_v2 c
    LEFT JOIN cart_items_v2 ci ON c.id = ci.cartId
    WHERE ci.cartId IS NULL;
END;
$$ LANGUAGE plpgsql;
