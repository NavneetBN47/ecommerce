-- Schema validation queries for LLD compliance

-- Verify users table structure
SELECT 
    'users_table_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'users' 
            AND column_name IN ('user_id', 'email', 'password_hash', 'created_at')
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify products table structure  
SELECT 
    'products_table_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'products' 
            AND column_name IN ('product_id', 'product_name', 'price', 'available_qty', 'is_active')
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify cart table structure
SELECT 
    'cart_table_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'cart' 
            AND column_name IN ('cart_id', 'user_id', 'created_at', 'updated_at')
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify cart_items table structure
SELECT 
    'cart_items_table_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'cart_items' 
            AND column_name IN ('cart_item_id', 'cart_id', 'product_id', 'quantity', 'price_at_addition')
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify constraints exist
SELECT 
    'constraints_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.table_constraints 
            WHERE constraint_name IN ('users_email_unique', 'products_price_check', 'cart_items_qty_check')
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;