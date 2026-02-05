-- =========================
-- USERS TABLE
-- =========================
CREATE TABLE users (
    user_id    SERIAL PRIMARY KEY,
    username   VARCHAR(50) UNIQUE NOT NULL,
    password   VARCHAR(100) NOT NULL,
    full_name  VARCHAR(100),
    email      VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- PRODUCTS TABLE
-- =========================
CREATE TABLE products (
    product_id    SERIAL PRIMARY KEY,
    product_name  VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    price         DECIMAL(10,2) NOT NULL,
    available_qty INT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- CART TABLE (1:1 with users)
-- =========================
CREATE TABLE cart (
    cart_id    SERIAL PRIMARY KEY,
    user_id    INT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
);

-- =========================
-- CART ITEMS TABLE (N:M resolved)
-- =========================
CREATE TABLE cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    cart_id      INT NOT NULL,
    product_id   INT NOT NULL,
    quantity     INT NOT NULL,
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES cart(cart_id),
    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(product_id)
);
