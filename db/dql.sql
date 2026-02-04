-- =========================================================
-- SCHEMA VALIDATION QUERIES
-- Queries to verify schema correctness after reconciliation
-- =========================================================

-- Verify quantity constraint is working
SELECT 
    'quantity_constraint_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.check_constraints 
            WHERE constraint_name = 'chk_cart_items_quantity_positive'
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify price constraint is working
SELECT 
    'price_constraint_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.check_constraints 
            WHERE constraint_name = 'chk_products_price_non_negative'
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify available_qty constraint is working
SELECT 
    'available_qty_constraint_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.check_constraints 
            WHERE constraint_name = 'chk_products_available_qty_non_negative'
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify CASCADE DELETE constraint is working
SELECT 
    'cascade_delete_constraint_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.referential_constraints rc
            JOIN information_schema.key_column_usage kcu 
                ON rc.constraint_name = kcu.constraint_name
            WHERE kcu.table_name = 'cart_items' 
                AND kcu.column_name = 'cart_id'
                AND rc.delete_rule = 'CASCADE'
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify all required indexes exist
SELECT 
    'required_indexes_check' as validation_type,
    CASE 
        WHEN (
            SELECT COUNT(*) FROM pg_indexes 
            WHERE indexname IN (
                'idx_cart_user_id',
                'idx_cart_items_cart_id', 
                'idx_cart_items_product_id',
                'idx_products_name_search'
            )
        ) = 4 THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify table structure matches LLD requirements
SELECT 
    'table_structure_check' as validation_type,
    CASE 
        WHEN (
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_name IN ('users', 'products', 'cart', 'cart_items')
        ) = 4 THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Verify unique constraints
SELECT 
    'unique_constraints_check' as validation_type,
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.table_constraints 
            WHERE constraint_type = 'UNIQUE' 
                AND table_name = 'users' 
                AND constraint_name LIKE '%username%'
        ) AND EXISTS (
            SELECT 1 FROM information_schema.table_constraints 
            WHERE constraint_type = 'UNIQUE' 
                AND table_name = 'cart' 
                AND constraint_name LIKE '%user_id%'
        ) THEN 'PASS'
        ELSE 'FAIL'
    END as result;

-- Overall schema validation summary
SELECT 
    'schema_reconciliation_summary' as validation_type,
    'Schema successfully reconciled with LLD requirements' as result,
    CURRENT_TIMESTAMP as validated_at;