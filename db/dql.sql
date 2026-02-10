-- =========================
-- DQL FOR CART MANAGEMENT SYSTEM (LLD as authoritative)
-- =========================
-- Validation: View cart and items
SELECT
    c.cart_id,
    c.user_id,
    c.created_at,
    c.updated_at,
    ci.item_id,
    ci.product_id,
    ci.quantity,
    ci.price,
    ci.added_at
FROM carts c
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id;

-- Validation: Count items per cart
SELECT cart_id, COUNT(*) AS item_count FROM cart_items GROUP BY cart_id;

-- Validation: Foreign key integrity
SELECT ci.cart_id FROM cart_items ci LEFT JOIN carts c ON ci.cart_id = c.cart_id WHERE c.cart_id IS NULL;
SELECT ci.product_id FROM cart_items ci LEFT JOIN products p ON ci.product_id = p.product_id WHERE p.product_id IS NULL;
