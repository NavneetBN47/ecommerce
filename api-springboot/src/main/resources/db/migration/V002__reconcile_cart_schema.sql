-- Migration V002: Reconcile Cart Schema with LLD Requirements
-- Ensures one-to-one relationship between User and Cart
-- Removes session-based carts to align with LLD stateless authentication

-- Drop session_id column (not required by LLD, carts are user-specific only)
ALTER TABLE shopping_carts DROP COLUMN IF EXISTS session_id;

-- Drop expires_at column (LLD specifies carts deleted on logout, no expiration)
ALTER TABLE shopping_carts DROP COLUMN IF EXISTS expires_at;

-- Make user_id NOT NULL (every cart must belong to a user per LLD)
ALTER TABLE shopping_carts ALTER COLUMN user_id SET NOT NULL;

-- Ensure unique constraint on user_id (one cart per user per LLD)
ALTER TABLE shopping_carts DROP CONSTRAINT IF EXISTS shopping_carts_user_id_key;
ALTER TABLE shopping_carts ADD CONSTRAINT shopping_carts_user_id_key UNIQUE (user_id);

-- Remove check constraint that allowed session_id
ALTER TABLE shopping_carts DROP CONSTRAINT IF EXISTS chk_cart_owner;

-- Add comment to document schema reconciliation
COMMENT ON TABLE shopping_carts IS 'Shopping carts - one per user, deleted on logout (LLD requirement)';
COMMENT ON COLUMN shopping_carts.user_id IS 'Foreign key to users table - one-to-one relationship (LLD requirement)';