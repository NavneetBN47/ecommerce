-- Migration script to add username column to users table
-- This reconciles the LLD requirement for username field

-- Add username column
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(50);

-- Create unique index on username
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Update existing users to have username same as email prefix (temporary)
UPDATE users 
SET username = SPLIT_PART(email, '@', 1)
WHERE username IS NULL;

-- Make username NOT NULL after populating
ALTER TABLE users ALTER COLUMN username SET NOT NULL;

-- Add comment
COMMENT ON COLUMN users.username IS 'Unique username for user login (immutable)';