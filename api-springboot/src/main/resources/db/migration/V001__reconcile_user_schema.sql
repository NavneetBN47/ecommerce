-- Migration V001: Reconcile User Schema with LLD Requirements
-- Adds username column and full_name column to align with LLD specification

-- Add username column (unique, immutable, required by LLD)
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(255) UNIQUE;

-- Add full_name column (required by LLD)
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);

-- Update existing records to populate username from email (temporary migration)
UPDATE users SET username = email WHERE username IS NULL;

-- Update existing records to populate full_name from first_name and last_name
UPDATE users SET full_name = CONCAT(first_name, ' ', last_name) WHERE full_name IS NULL;

-- Make username NOT NULL after populating data
ALTER TABLE users ALTER COLUMN username SET NOT NULL;

-- Make full_name NOT NULL after populating data
ALTER TABLE users ALTER COLUMN full_name SET NOT NULL;

-- Create index on username for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Add comment to document schema reconciliation
COMMENT ON COLUMN users.username IS 'Unique username for user login (LLD requirement)';
COMMENT ON COLUMN users.full_name IS 'Full name of user (LLD requirement)';