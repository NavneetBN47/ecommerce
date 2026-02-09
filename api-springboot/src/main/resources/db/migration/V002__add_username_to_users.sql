-- Migration script to add username field to users table
-- This reconciles the LLD requirement for username with existing email-based schema

ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(100);

-- Make username unique and not null after data migration
-- First, populate username from email for existing users
UPDATE users SET username = SPLIT_PART(email, '@', 1) WHERE username IS NULL;

-- Now add constraints
ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_username_unique UNIQUE (username);

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Add comment
COMMENT ON COLUMN users.username IS 'Unique username for user login, immutable after creation';