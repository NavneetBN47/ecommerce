-- Migration script to optimize product search per LLD requirements

-- Add GIN index for case-insensitive product search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create indexes for case-insensitive search on product name and description
CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING gin (LOWER(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_description_trgm ON products USING gin (LOWER(description) gin_trgm_ops);

-- Add functional index for case-insensitive name search
CREATE INDEX IF NOT EXISTS idx_products_name_lower ON products (LOWER(name));

-- Add comment
COMMENT ON INDEX idx_products_name_trgm IS 'Supports case-insensitive product search per LLD requirement';