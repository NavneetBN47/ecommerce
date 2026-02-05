-- =========================
-- CARTS TABLE
-- =========================
CREATE TABLE carts (
    cart_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_active_cart_per_user UNIQUE (user_id, status)
);

-- =========================
-- CART ITEMS TABLE
-- =========================
CREATE TABLE cart_items (
    item_id UUID PRIMARY KEY,
    cart_id UUID REFERENCES carts(cart_id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_addition DECIMAL(10,2) NOT NULL
);

-- =========================
-- AUDIT LOG TABLE
-- =========================
CREATE TABLE cart_audit_log (
    log_id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by UUID NOT NULL,
    performed_at TIMESTAMP NOT NULL,
    details TEXT
);

-- =========================
-- TRIGGERS & FUNCTIONS
-- =========================
-- Automatically update updated_at on carts
CREATE OR REPLACE FUNCTION update_cart_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_cart_timestamp
BEFORE UPDATE ON carts
FOR EACH ROW
EXECUTE PROCEDURE update_cart_timestamp();

-- Extend cart expiration on activity
CREATE OR REPLACE FUNCTION extend_cart_expiration()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE carts SET expires_at = CURRENT_TIMESTAMP + INTERVAL '30 minutes'
    WHERE cart_id = NEW.cart_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_extend_cart_expiration
AFTER INSERT OR UPDATE ON cart_items
FOR EACH ROW
EXECUTE PROCEDURE extend_cart_expiration();
