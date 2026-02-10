-- V002__seed_data.sql
-- Seed data for e-commerce application

-- Insert sample products
INSERT INTO products (name, description, sku, price, stock_quantity, category, image_url, active) VALUES
('Laptop Pro 15', 'High-performance laptop with 15-inch display, 16GB RAM, 512GB SSD', 'LAPTOP-PRO-15', 1299.99, 50, 'Electronics', 'https://example.com/images/laptop-pro-15.jpg', true),
('Wireless Mouse', 'Ergonomic wireless mouse with precision tracking', 'MOUSE-WL-001', 29.99, 200, 'Electronics', 'https://example.com/images/wireless-mouse.jpg', true),
('Mechanical Keyboard', 'RGB mechanical keyboard with Cherry MX switches', 'KEYBOARD-MX-001', 149.99, 100, 'Electronics', 'https://example.com/images/mechanical-keyboard.jpg', true),
('USB-C Hub', '7-in-1 USB-C hub with HDMI, USB 3.0, and SD card reader', 'HUB-USBC-001', 49.99, 150, 'Electronics', 'https://example.com/images/usbc-hub.jpg', true),
('Laptop Stand', 'Adjustable aluminum laptop stand', 'STAND-LAPTOP-001', 39.99, 80, 'Accessories', 'https://example.com/images/laptop-stand.jpg', true),
('Wireless Headphones', 'Noise-cancelling wireless headphones with 30-hour battery', 'HEADPHONE-WL-001', 199.99, 75, 'Electronics', 'https://example.com/images/wireless-headphones.jpg', true),
('Smartphone X', 'Latest smartphone with 6.5-inch OLED display, 128GB storage', 'PHONE-X-128', 899.99, 120, 'Electronics', 'https://example.com/images/smartphone-x.jpg', true),
('Tablet Pro', '11-inch tablet with stylus support, 256GB storage', 'TABLET-PRO-256', 699.99, 60, 'Electronics', 'https://example.com/images/tablet-pro.jpg', true),
('Smartwatch Series 5', 'Fitness tracking smartwatch with heart rate monitor', 'WATCH-S5', 299.99, 90, 'Electronics', 'https://example.com/images/smartwatch-s5.jpg', true),
('Portable Charger', '20000mAh portable charger with fast charging', 'CHARGER-20K', 39.99, 200, 'Accessories', 'https://example.com/images/portable-charger.jpg', true),
('Webcam HD', '1080p HD webcam with built-in microphone', 'WEBCAM-HD-001', 79.99, 110, 'Electronics', 'https://example.com/images/webcam-hd.jpg', true),
('External SSD 1TB', 'Portable external SSD with 1TB storage', 'SSD-EXT-1TB', 129.99, 85, 'Electronics', 'https://example.com/images/external-ssd-1tb.jpg', true),
('Monitor 27"', '27-inch 4K monitor with HDR support', 'MONITOR-27-4K', 449.99, 40, 'Electronics', 'https://example.com/images/monitor-27-4k.jpg', true),
('Desk Lamp LED', 'Adjustable LED desk lamp with touch control', 'LAMP-LED-001', 34.99, 130, 'Accessories', 'https://example.com/images/desk-lamp-led.jpg', true),
('Cable Organizer', 'Cable management organizer set', 'ORGANIZER-CABLE', 14.99, 250, 'Accessories', 'https://example.com/images/cable-organizer.jpg', true);

COMMENT ON TABLE products IS 'Sample products for testing and demonstration';