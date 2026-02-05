-- =========================================================
-- DML SCRIPT FOR ECOMMERCE BROWNFIELD APPLICATION
-- PostgreSQL Compatible
-- =========================================================
-- Contains:
-- 1. Seed data (users, products)
-- 2. Application-level data mutations
-- Cart lifecycle is LAZY and EPHEMERAL
-- =========================================================


-- =========================================================
-- SECTION 1: SEED DATA
-- =========================================================


-- ---------------------------------------------------------
-- 1. USERS (Seed Data)
-- ---------------------------------------------------------
-- No carts created for seed users
-- ---------------------------------------------------------

INSERT INTO users (username, password, full_name, email) VALUES
('mickey', 'password123', 'Mickey Mouse', 'mickey@cartoon.com'),
('minnie', 'password123', 'Minnie Mouse', 'minnie@cartoon.com'),
('donald', 'password123', 'Donald Duck', 'donald@cartoon.com'),
('goofy',  'password123', 'Goofy', 'goofy@cartoon.com'),
('pluto',  'password123', 'Pluto', 'pluto@cartoon.com');


-- ---------------------------------------------------------
-- 2. PRODUCTS (Seed Data)
-- ---------------------------------------------------------

INSERT INTO products (product_name, description, price, available_qty) VALUES
('Laptop', 'High performance laptop', 1200.00, 10),
('Mouse', 'Wireless mouse', 25.00, 50),
('Keyboard', 'Mechanical keyboard', 80.00, 30),
('Monitor', '24 inch LED monitor', 180.00, 20),
('Headphones', 'Noise cancelling headphones', 150.00, 15),
('Webcam', 'HD webcam', 70.00, 25),
('Printer', 'All-in-one printer', 200.00, 10),
('Desk Lamp', 'LED desk lamp', 35.00, 40),
('USB Hub', '4-port USB hub', 20.00, 60),
('External HDD', '1TB external hard drive', 90.00, 18),
('SSD', '512GB solid state drive', 110.00, 22),
('Router', 'Wireless router', 95.00, 16),
('Smartphone', 'Android smartphone', 650.00, 12),
('Tablet', '10 inch tablet', 400.00, 14),
('Power Bank', '10000mAh power bank', 30.00, 45),
('Bluetooth Speaker', 'Portable speaker', 55.00, 28),
('Smart Watch', 'Fitness smart watch', 220.00, 10),
('Camera', 'Digital camera', 500.00, 8),
('Microphone', 'USB microphone', 85.00, 20),
('Gaming Chair', 'Ergonomic gaming chair', 300.00, 6);


-- =========================================================
-- SECTION 2: APPLICATION DML OPERATIONS
-- =========================================================


-- ---------------------------------------------------------
-- 3. USER SIGN-UP
-- ---------------------------------------------------------
-- NO cart creation at signup
-- ---------------------------------------------------------

INSERT INTO users (username, password, full_name, email)
VALUES (?, ?, ?, ?);


-- ---------------------------------------------------------
-- 4. CREATE CART (Lazy Creation)
-- ---------------------------------------------------------
-- Called ONLY when first product is added
-- ---------------------------------------------------------

INSERT INTO cart (user_id)
VALUES (?);


-- ---------------------------------------------------------
-- 5. ADD PRODUCT TO CART
-- ---------------------------------------------------------

INSERT INTO cart_items (cart_id, product_id, quantity)
VALUES (?, ?, ?);


-- ---------------------------------------------------------
-- 6. UPDATE CART ITEM QUANTITY
-- ---------------------------------------------------------

UPDATE cart_items
SET quantity = ?
WHERE cart_id = ? AND product_id = ?;


-- ---------------------------------------------------------
-- 7. REMOVE PRODUCT FROM CART
-- ---------------------------------------------------------

DELETE FROM cart_items
WHERE cart_id = ? AND product_id = ?;


-- ---------------------------------------------------------
-- 8. DELETE CART WHEN EMPTY
-- ---------------------------------------------------------
-- Called when last item is removed
-- ---------------------------------------------------------

DELETE FROM cart
WHERE cart_id = ?;


-- ---------------------------------------------------------
-- 9. USER LOGOUT – CLEAR CART
-- ---------------------------------------------------------
-- Clears cart + cart items on logout
-- ---------------------------------------------------------

DELETE FROM cart_items
WHERE cart_id = ?;

DELETE FROM cart
WHERE cart_id = ?;


-- ---------------------------------------------------------
-- 10. USER PROFILE UPDATE
-- ---------------------------------------------------------

UPDATE users
SET full_name = ?,
    email = ?
WHERE user_id = ?;


-- =========================================================
-- END OF DML SCRIPT
-- =========================================================
