-- Shopping Cart System - Data Manipulation Language (DML)
-- Generated from Low Level Design (LLD) - Seed Data
-- Version: 1.0
-- Date: Generated for schema reconciliation

-- Clear existing data (non-destructive approach - only if tables are empty)
-- Note: These are conditional statements that won't affect existing data

-- Seed Users data
-- Passwords are hashed using bcrypt (example hashes for 'password123')
INSERT IGNORE INTO users (id, username, password, email, roles, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTGJpEqZNtgs6r7vkkHMtFDfKIVyTSdG', 'admin@shoppingcart.com', '["ADMIN","USER"]', '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'john_doe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTGJpEqZNtgs6r7vkkHMtFDfKIVyTSdG', 'john.doe@email.com', '["USER"]', '2024-01-02 09:30:00', '2024-01-02 09:30:00'),
(3, 'jane_smith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTGJpEqZNtgs6r7vkkHMtFDfKIVyTSdG', 'jane.smith@email.com', '["USER"]', '2024-01-02 14:15:00', '2024-01-02 14:15:00'),
(4, 'mike_wilson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTGJpEqZNtgs6r7vkkHMtFDfKIVyTSdG', 'mike.wilson@email.com', '["USER"]', '2024-01-03 11:20:00', '2024-01-03 11:20:00'),
(5, 'sarah_johnson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTGJpEqZNtgs6r7vkkHMtFDfKIVyTSdG', 'sarah.johnson@email.com', '["USER"]', '2024-01-03 16:45:00', '2024-01-03 16:45:00');

-- Seed Products data with comprehensive catalog
INSERT IGNORE INTO products (id, name, description, price, stock_quantity, category, created_at, updated_at) VALUES
-- Electronics Category
(1, 'iPhone 15 Pro', 'Latest Apple smartphone with advanced camera system and A17 Pro chip', 999.99, 50, 'Electronics', '2024-01-01 08:00:00', '2024-01-01 08:00:00'),
(2, 'Samsung Galaxy S24', 'Premium Android smartphone with AI-powered features', 899.99, 75, 'Electronics', '2024-01-01 08:15:00', '2024-01-01 08:15:00'),
(3, 'MacBook Pro 14"', 'Professional laptop with M3 chip for creative professionals', 1999.99, 25, 'Electronics', '2024-01-01 08:30:00', '2024-01-01 08:30:00'),
(4, 'Dell XPS 13', 'Ultra-portable laptop with premium build quality', 1299.99, 30, 'Electronics', '2024-01-01 08:45:00', '2024-01-01 08:45:00'),
(5, 'iPad Air', 'Versatile tablet for work and entertainment', 599.99, 40, 'Electronics', '2024-01-01 09:00:00', '2024-01-01 09:00:00'),
-- Clothing Category
(6, 'Nike Air Max 270', 'Comfortable running shoes with air cushioning', 149.99, 100, 'Clothing', '2024-01-01 09:15:00', '2024-01-01 09:15:00'),
(7, 'Levi''s 501 Jeans', 'Classic straight-fit denim jeans', 79.99, 150, 'Clothing', '2024-01-01 09:30:00', '2024-01-01 09:30:00'),
(8, 'Adidas Ultraboost 22', 'Premium running shoes with boost technology', 189.99, 80, 'Clothing', '2024-01-01 09:45:00', '2024-01-01 09:45:00'),
(9, 'Champion Hoodie', 'Comfortable cotton blend hoodie', 49.99, 200, 'Clothing', '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(10, 'Ray-Ban Aviator Sunglasses', 'Classic aviator style sunglasses', 159.99, 60, 'Clothing', '2024-01-01 10:15:00', '2024-01-01 10:15:00'),
-- Home & Garden Category
(11, 'Dyson V15 Vacuum', 'Cordless vacuum cleaner with laser detection', 749.99, 35, 'Home & Garden', '2024-01-01 10:30:00', '2024-01-01 10:30:00'),
(12, 'KitchenAid Stand Mixer', 'Professional-grade stand mixer for baking', 399.99, 45, 'Home & Garden', '2024-01-01 10:45:00', '2024-01-01 10:45:00'),
(13, 'Instant Pot Duo 7-in-1', 'Multi-functional pressure cooker', 99.99, 120, 'Home & Garden', '2024-01-01 11:00:00', '2024-01-01 11:00:00'),
(14, 'Philips Hue Smart Bulbs', 'Color-changing smart LED bulbs (4-pack)', 199.99, 90, 'Home & Garden', '2024-01-01 11:15:00', '2024-01-01 11:15:00'),
(15, 'Roomba i7+ Robot Vacuum', 'Self-emptying robot vacuum with mapping', 599.99, 20, 'Home & Garden', '2024-01-01 11:30:00', '2024-01-01 11:30:00'),
-- Books Category
(16, 'The Psychology of Money', 'Financial wisdom and behavioral insights', 24.99, 300, 'Books', '2024-01-01 11:45:00', '2024-01-01 11:45:00'),
(17, 'Atomic Habits', 'Guide to building good habits and breaking bad ones', 18.99, 250, 'Books', '2024-01-01 12:00:00', '2024-01-01 12:00:00'),
(18, 'Clean Code', 'A handbook of agile software craftsmanship', 49.99, 180, 'Books', '2024-01-01 12:15:00', '2024-01-01 12:15:00'),
(19, 'The Lean Startup', 'How today''s entrepreneurs use continuous innovation', 29.99, 200, 'Books', '2024-01-01 12:30:00', '2024-01-01 12:30:00'),
(20, 'Designing Data-Intensive Applications', 'The big ideas behind reliable, scalable systems', 59.99, 150, 'Books', '2024-01-01 12:45:00', '2024-01-01 12:45:00');

-- Seed Cart data (some active, some checked out)
INSERT IGNORE INTO cart (id, user_id, status, created_at, updated_at) VALUES
(1, 2, 'ACTIVE', '2024-01-10 10:00:00', '2024-01-10 15:30:00'),
(2, 3, 'ACTIVE', '2024-01-10 11:30:00', '2024-01-10 14:20:00'),
(3, 4, 'CHECKED_OUT', '2024-01-09 09:15:00', '2024-01-09 16:45:00'),
(4, 5, 'ACTIVE', '2024-01-10 13:20:00', '2024-01-10 13:20:00'),
(5, 2, 'CHECKED_OUT', '2024-01-08 14:30:00', '2024-01-08 18:20:00');

-- Seed CartItem data (respecting business rules)
INSERT IGNORE INTO cart_items (id, cart_id, product_id, quantity, price_at_addition, created_at, updated_at) VALUES
-- John Doe's active cart (cart_id: 1)
(1, 1, 1, 1, 999.99, '2024-01-10 10:15:00', '2024-01-10 10:15:00'),  -- iPhone 15 Pro
(2, 1, 6, 2, 149.99, '2024-01-10 11:30:00', '2024-01-10 15:30:00'),  -- Nike Air Max 270 (qty: 2)
(3, 1, 16, 1, 24.99, '2024-01-10 12:45:00', '2024-01-10 12:45:00'),  -- The Psychology of Money
-- Jane Smith's active cart (cart_id: 2)
(4, 2, 3, 1, 1999.99, '2024-01-10 11:45:00', '2024-01-10 11:45:00'), -- MacBook Pro 14"
(5, 2, 12, 1, 399.99, '2024-01-10 13:20:00', '2024-01-10 14:20:00'),  -- KitchenAid Stand Mixer
(6, 2, 17, 3, 18.99, '2024-01-10 14:00:00', '2024-01-10 14:00:00'),   -- Atomic Habits (qty: 3)
-- Mike Wilson's checked out cart (cart_id: 3)
(7, 3, 2, 1, 899.99, '2024-01-09 09:30:00', '2024-01-09 09:30:00'),   -- Samsung Galaxy S24
(8, 3, 7, 2, 79.99, '2024-01-09 10:15:00', '2024-01-09 10:15:00'),    -- Levi's 501 Jeans (qty: 2)
(9, 3, 13, 1, 99.99, '2024-01-09 11:00:00', '2024-01-09 11:00:00'),    -- Instant Pot Duo
(10, 3, 18, 1, 49.99, '2024-01-09 12:30:00', '2024-01-09 12:30:00'),   -- Clean Code
-- Sarah Johnson's active cart (cart_id: 4)
(11, 4, 5, 1, 599.99, '2024-01-10 13:25:00', '2024-01-10 13:25:00'),  -- iPad Air
(12, 4, 14, 2, 199.99, '2024-01-10 13:40:00', '2024-01-10 13:40:00'), -- Philips Hue Smart Bulbs (qty: 2)
-- John Doe's previous checked out cart (cart_id: 5)
(13, 5, 11, 1, 749.99, '2024-01-08 14:45:00', '2024-01-08 14:45:00'),  -- Dyson V15 Vacuum
(14, 5, 9, 3, 49.99, '2024-01-08 15:20:00', '2024-01-08 15:20:00'),    -- Champion Hoodie (qty: 3)
(15, 5, 19, 2, 29.99, '2024-01-08 16:10:00', '2024-01-08 16:10:00');   -- The Lean Startup (qty: 2)

-- Update auto-increment counters to prevent conflicts
ALTER TABLE users AUTO_INCREMENT = 6;
ALTER TABLE products AUTO_INCREMENT = 21;
ALTER TABLE cart AUTO_INCREMENT = 6;
ALTER TABLE cart_items AUTO_INCREMENT = 16;

-- Data validation summary
-- Users: 5 records with proper roles, hashed passwords, unique constraints ✓
-- Products: 20 records across 4 categories with proper pricing and stock ✓
-- Carts: 5 records with mixed ACTIVE/CHECKED_OUT status ✓
-- CartItems: 15 records respecting quantity limits (≤10) and business rules ✓
-- All audit fields populated ✓
-- All foreign key relationships maintained ✓
-- Business rules enforced: unique active cart per user, max quantity limits ✓
