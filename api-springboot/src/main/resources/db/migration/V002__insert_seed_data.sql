-- V002__insert_seed_data.sql
-- Seed data for E-Commerce application

-- Insert default categories
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Men and women clothing'),
('Books', 'Physical and digital books'),
('Home & Garden', 'Home improvement and garden supplies'),
('Sports', 'Sports equipment and accessories');

-- Insert admin user (password: Admin@123)
-- Password is BCrypt hashed with strength 12
INSERT INTO users (username, email, password, first_name, last_name, role) VALUES
('admin', 'admin@ecommerce.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIR.u1DkqK', 'Admin', 'User', 'ADMIN');

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url) VALUES
('Laptop Pro 15', 'High-performance laptop with 16GB RAM and 512GB SSD. Perfect for professionals and gamers.', 1299.99, 50, 1, 'https://example.com/laptop.jpg'),
('Wireless Mouse', 'Ergonomic wireless mouse with precision tracking and long battery life.', 29.99, 200, 1, 'https://example.com/mouse.jpg'),
('Mechanical Keyboard', 'RGB backlit mechanical keyboard with customizable keys.', 89.99, 150, 1, 'https://example.com/keyboard.jpg'),
('USB-C Hub', '7-in-1 USB-C hub with HDMI, USB 3.0, and card reader.', 45.99, 100, 1, 'https://example.com/hub.jpg'),
('Wireless Headphones', 'Noise-cancelling Bluetooth headphones with 30-hour battery.', 199.99, 75, 1, 'https://example.com/headphones.jpg'),
('Cotton T-Shirt', 'Comfortable 100% cotton t-shirt available in multiple colors.', 19.99, 500, 2, 'https://example.com/tshirt.jpg'),
('Denim Jeans', 'Classic fit denim jeans with stretch fabric.', 59.99, 300, 2, 'https://example.com/jeans.jpg'),
('Hoodie', 'Warm fleece hoodie with front pocket.', 39.99, 250, 2, 'https://example.com/hoodie.jpg'),
('Running Shoes', 'Lightweight running shoes with cushioned sole.', 79.99, 200, 2, 'https://example.com/shoes.jpg'),
('Winter Jacket', 'Insulated winter jacket with water-resistant coating.', 149.99, 100, 2, 'https://example.com/jacket.jpg'),
('Java Programming Book', 'Complete guide to Java programming with practical examples.', 49.99, 100, 3, 'https://example.com/java-book.jpg'),
('Python for Beginners', 'Learn Python programming from scratch with hands-on projects.', 39.99, 120, 3, 'https://example.com/python-book.jpg'),
('Web Development Guide', 'Master HTML, CSS, and JavaScript for modern web development.', 44.99, 90, 3, 'https://example.com/web-book.jpg'),
('Data Science Handbook', 'Comprehensive guide to data science and machine learning.', 59.99, 80, 3, 'https://example.com/ds-book.jpg'),
('Clean Code', 'Best practices for writing maintainable and efficient code.', 54.99, 110, 3, 'https://example.com/clean-code.jpg'),
('Garden Tools Set', 'Complete set of essential garden tools including spade, rake, and pruner.', 79.99, 75, 4, 'https://example.com/tools.jpg'),
('Plant Pots Set', 'Set of 5 decorative ceramic plant pots in various sizes.', 34.99, 150, 4, 'https://example.com/pots.jpg'),
('Garden Hose', '50-foot expandable garden hose with spray nozzle.', 29.99, 100, 4, 'https://example.com/hose.jpg'),
('LED Solar Lights', 'Set of 8 solar-powered garden pathway lights.', 39.99, 120, 4, 'https://example.com/lights.jpg'),
('Lawn Mower', 'Electric lawn mower with adjustable cutting height.', 249.99, 40, 4, 'https://example.com/mower.jpg'),
('Tennis Racket', 'Professional-grade tennis racket with carbon fiber frame.', 129.99, 30, 5, 'https://example.com/racket.jpg'),
('Yoga Mat', 'Non-slip yoga mat with carrying strap.', 24.99, 200, 5, 'https://example.com/yoga-mat.jpg'),
('Dumbbell Set', 'Adjustable dumbbell set from 5 to 50 lbs.', 199.99, 50, 5, 'https://example.com/dumbbells.jpg'),
('Basketball', 'Official size basketball with superior grip.', 34.99, 150, 5, 'https://example.com/basketball.jpg'),
('Fitness Tracker', 'Smart fitness tracker with heart rate monitor and GPS.', 89.99, 100, 5, 'https://example.com/tracker.jpg');