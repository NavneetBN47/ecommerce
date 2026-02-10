-- =========================
-- DDL FOR CART MANAGEMENT SYSTEM (LLD as authoritative)
-- =========================
CREATE TABLE IF NOT EXISTS carts (
    cart_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cart_user UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    item_id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 1),
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Reference table for products (as per LLD, not managed here)
-- CREATE TABLE IF NOT EXISTS products (...)
-- If not present, must be created externally.

-- Indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);
