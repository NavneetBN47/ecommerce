-- Migration V003: Reconcile Product Schema with LLD Requirements
-- Adds available_qty column and removes unnecessary fields

-- Add available_qty column (required by LLD)
ALTER TABLE products ADD COLUMN IF NOT EXISTS available_qty INTEGER DEFAULT 0;

-- Update available_qty from product_inventory if exists
UPDATE products p
SET available_qty = COALESCE(
    (SELECT pi.quantity_available FROM product_inventory pi WHERE pi.product_id = p.product_id),
    0
)
WHERE available_qty IS NULL OR available_qty = 0;

-- Make available_qty NOT NULL and add check constraint
ALTER TABLE products ALTER COLUMN available_qty SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT chk_available_qty_positive CHECK (available_qty >= 0);

-- Drop category_id NOT NULL constraint (LLD doesn't specify mandatory category)
ALTER TABLE products ALTER COLUMN category_id DROP NOT NULL;

-- Add comment to document schema reconciliation
COMMENT ON COLUMN products.available_qty IS 'Available quantity for product (LLD requirement)';