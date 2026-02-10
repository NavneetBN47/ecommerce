-- Migration V003: Add Table and Column Comments for Documentation

-- Table comments
COMMENT ON TABLE cart IS 'Main cart entity storing user shopping cart information';
COMMENT ON TABLE cart_item IS 'Individual items within a shopping cart';
COMMENT ON TABLE cart_history IS 'Audit trail for cart operations and changes';
COMMENT ON TABLE users IS 'System users who can create and manage carts';
COMMENT ON TABLE products IS 'Products available for purchase';

-- Cart table column comments
COMMENT ON COLUMN cart.cart_status IS 'Current status of the cart: active, abandoned, converted, expired';
COMMENT ON COLUMN cart.expires_at IS 'Timestamp when the cart expires and becomes inactive';
COMMENT ON COLUMN cart.total_amount IS 'Total monetary value of all items in cart';
COMMENT ON COLUMN cart.total_items IS 'Total quantity of all items in cart';

-- CartItem table column comments
COMMENT ON COLUMN cart_item.total_price IS 'Computed column: quantity * unit_price';
COMMENT ON COLUMN cart_item.quantity IS 'Quantity of this product in the cart';

-- CartHistory table column comments
COMMENT ON COLUMN cart_history.action_type IS 'Type of action performed: created, item_added, item_removed, item_updated, status_changed, expired';
COMMENT ON COLUMN cart_history.action_details IS 'JSON details of the action performed';