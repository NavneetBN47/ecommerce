-- DML Script for Shopping Cart System
-- Seed data as per LLD requirements
-- Additive and non-destructive operations only

-- Insert seed users (no carts as per LLD requirement)
INSERT IGNORE INTO users (username, email, password_hash, first_name, last_name) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'John', 'Doe'),
('jane_smith', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Jane', 'Smith'),
('bob_wilson', 'bob.wilson@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Bob', 'Wilson'),
('alice_brown', 'alice.brown@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Alice', 'Brown'),
('charlie_davis', 'charlie.davis@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'Charlie', 'Davis');

-- Insert seed products
INSERT IGNORE INTO products (product_name, description, price, stock_quantity, category) VALUES
('Laptop Computer', 'High-performance laptop for work and gaming', 999.99, 50, 'Electronics'),
('Wireless Mouse', 'Ergonomic wireless mouse with long battery life', 29.99, 200, 'Electronics'),
('Office Chair', 'Comfortable ergonomic office chair', 199.99, 25, 'Furniture'),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 49.99, 75, 'Furniture'),
('Coffee Mug', 'Ceramic coffee mug with company logo', 12.99, 500, 'Office Supplies'),
('Notebook Set', 'Set of 3 premium notebooks', 24.99, 100, 'Office Supplies'),
('Smartphone', 'Latest model smartphone with advanced features', 699.99, 30, 'Electronics'),
('Tablet', '10-inch tablet for productivity and entertainment', 399.99, 40, 'Electronics'),
('Wireless Headphones', 'Noise-cancelling wireless headphones', 149.99, 60, 'Electronics'),
('Standing Desk', 'Height-adjustable standing desk', 299.99, 15, 'Furniture');

-- Note: No seed carts or cart items as per LLD requirements
-- Carts are created dynamically when users add items
-- Seed users do not receive pre-populated carts

-- Update stock quantities for some products (additive operation)
UPDATE products SET stock_quantity = stock_quantity + 10 WHERE product_name IN ('Laptop Computer', 'Smartphone');

-- Ensure all seed products are active
UPDATE products SET is_active = TRUE WHERE product_id IN (
    SELECT product_id FROM (
        SELECT product_id FROM products WHERE product_name IN (
            'Laptop Computer', 'Wireless Mouse', 'Office Chair', 'Desk Lamp', 
            'Coffee Mug', 'Notebook Set', 'Smartphone', 'Tablet', 
            'Wireless Headphones', 'Standing Desk'
        )
    ) AS temp
);
