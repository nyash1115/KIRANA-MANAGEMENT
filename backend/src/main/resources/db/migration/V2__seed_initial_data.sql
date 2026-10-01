-- V2__seed_initial_data.sql: Realistic Kirana Store Sample Data

-- 1. Business
INSERT INTO businesses (id, name, trade_name, tax_number)
VALUES (1, 'Aapna Kirana Mart Private Limited', 'Aapna Super Kirana', 'AAACA9876Q')
ON CONFLICT (id) DO NOTHING;

-- 2. Store
INSERT INTO stores (id, business_id, name, code, address, city, state, pincode, phone, email, gstin, invoice_prefix, invoice_next_seq)
VALUES (1, 1, 'Main Market Branch', 'STR-001', 'Shop No. 12-14, Laxmi Road, Narayan Peth', 'Pune', 'Maharashtra', '411030', '9822001122', 'contact@aapnakirana.com', '27AABCU9603R1ZM', 'INV', 1001)
ON CONFLICT (id) DO NOTHING;

-- 3. Store Settings
INSERT INTO store_settings (store_id, tax_inclusive_pricing, enable_igst, allow_negative_stock, default_gst_rate, low_stock_threshold_default, expiry_alert_days_default, invoice_footer_message, invoice_terms)
VALUES (1, TRUE, FALSE, FALSE, 0.00, 5.000, 30, 'Thank you for shopping at Aapna Super Kirana! Visit again.', 'Goods once sold will only be exchanged within 3 days with original bill.')
ON CONFLICT (store_id) DO NOTHING;

-- 4. Users (Passwords: admin123, manager123, cashier123)
INSERT INTO users (id, business_id, store_id, username, password_hash, full_name, email, phone, role)
VALUES 
(1, 1, 1, 'admin', '$2a$10$SxN7XoD41mHLqe.wRl/TpO91CMlRyCqBv8hlmoBxY7QHzvoBs6NcW', 'Sunil Agarwal (Owner)', 'sunil@aapnakirana.com', '9822001100', 'ROLE_ADMIN'),
(2, 1, 1, 'manager', '$2a$10$7Uif99R7x/5xYST/XjajM.fI6IS3iURF1AgV9GZBxvFUD1FNOuXUi', 'Vikas Deshmukh (Manager)', 'vikas@aapnakirana.com', '9822001101', 'ROLE_MANAGER'),
(3, 1, 1, 'cashier', '$2a$10$bWVBNJVzVuqzTwI9vN8C2eL/JWRTDPaAskHrw0ePY5KlFQhp4Q/Ji', 'Pooja Kulkarni (Cashier)', 'pooja@aapnakirana.com', '9822001102', 'ROLE_CASHIER')
ON CONFLICT (id) DO NOTHING;

-- 5. Units
INSERT INTO units (id, name, code, allow_decimals)
VALUES 
(1, 'Piece', 'pc', FALSE),
(2, 'Packet', 'pkt', FALSE),
(3, 'Kilogram', 'kg', TRUE),
(4, 'Gram', 'g', TRUE),
(5, 'Liter', 'l', TRUE),
(6, 'Milliliter', 'ml', TRUE),
(7, 'Box', 'box', FALSE),
(8, 'Dozen', 'doz', FALSE)
ON CONFLICT (id) DO NOTHING;

-- 6. Categories
INSERT INTO categories (id, business_id, name, slug, description)
VALUES 
(1, 1, 'Grocery & Staples', 'grocery-staples', 'Atta, Rice, Dals, Pulses, Sugar, Edible Oils'),
(2, 1, 'Dairy & Bakery', 'dairy-bakery', 'Milk, Butter, Paneer, Bread, Cheese'),
(3, 1, 'Snacks & Packaged Food', 'snacks-packaged', 'Biscuits, Noodles, Chips, Namkeen, Chocolates'),
(4, 1, 'Beverages', 'beverages', 'Tea, Coffee, Fruit Juices, Soft Drinks'),
(5, 1, 'Spices & Masalas', 'spices-masalas', 'Whole & Powdered Spices, Cooking Pastes, Salt'),
(6, 1, 'Personal Care', 'personal-care', 'Soaps, Shampoos, Toothpastes, Skin Creams'),
(7, 1, 'Household & Cleaning', 'household-cleaning', 'Detergents, Dishwash, Floor Cleaners, Repellents')
ON CONFLICT (id) DO NOTHING;

-- 7. Suppliers
INSERT INTO suppliers (id, business_id, name, contact_person, phone, email, address, gstin, outstanding_balance)
VALUES 
(1, 1, 'Metro Cash & Carry Pune', 'Anand Joshi', '9822012345', 'metro.pune@fmcgdist.com', 'Plot 45, MIDC Bhosari, Pune', '27AACCM1234F1Z5', 15400.00),
(2, 1, 'ITC Distributors Maharashtra', 'Kishore Shinde', '9822098765', 'itc.sales@shindedist.com', 'Hadapsar Industrial Estate, Pune', '27AAACI5678K1Z8', 0.00),
(3, 1, 'Amul Super Stockist', 'Mahesh Patel', '9822045678', 'amul.stockist@puneagency.com', 'Market Yard, Gultekdi, Pune', '27AABCA9101L1Z2', 8200.00)
ON CONFLICT (id) DO NOTHING;

-- 8. Customers (With Udhaar/Khata balances)
INSERT INTO customers (id, business_id, name, phone, email, address, credit_limit, current_outstanding)
VALUES 
(1, 1, 'Ramesh Kumar', '9890112233', 'ramesh.k@gmail.com', 'Flat 402, Sai Residency, Narayan Peth', 10000.00, 450.00),
(2, 1, 'Sunita Patil', '9890445566', 'sunita.patil@outlook.com', '142, Shanipar Chowk, Sadashiv Peth', 5000.00, 0.00),
(3, 1, 'Rajesh Sharma', '9890778899', 'rajesh.sharma@yahoo.com', 'Shop 3, Near City Pride, Kothrud', 12000.00, 1250.00)
ON CONFLICT (id) DO NOTHING;

-- Initial Khata Ledger records for starting balances
INSERT INTO khata_transactions (id, store_id, customer_id, type, reference_type, reference_id, amount, balance_after, notes, created_by, transaction_date)
VALUES 
(1, 1, 1, 'DEBIT_SALE', 'SALE', 'PREV-OCT-092', 450.00, 450.00, 'Previous month groceries credit', 1, NOW() - INTERVAL '5 days'),
(2, 1, 3, 'DEBIT_SALE', 'SALE', 'PREV-OCT-115', 1250.00, 1250.00, 'Monthly ration purchase on credit', 1, NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

-- 9. Products
INSERT INTO products (id, business_id, category_id, unit_id, name, barcode, sku, brand, hsn_code, gst_rate, default_cost_price, default_selling_price, default_mrp, min_stock_level, reorder_level)
VALUES 
(1, 1, 1, 2, 'Aashirvaad Shudh Chakki Atta 5kg', '8901030012345', 'ATTA-AASH-5KG', 'ITC Aashirvaad', '1101', 0.00, 215.00, 245.00, 260.00, 5.000, 15.000),
(2, 1, 1, 5, 'Fortune Sunlite Sunflower Oil 1L', '8906007281014', 'OIL-FORT-1L', 'Fortune', '1512', 5.00, 132.00, 152.00, 165.00, 10.000, 25.000),
(3, 1, 5, 2, 'Tata Salt Vacuum Evaporated 1kg', '8901058852212', 'SALT-TATA-1KG', 'Tata Consumer', '2501', 0.00, 21.00, 26.00, 28.00, 15.000, 30.000),
(4, 1, 3, 2, 'Maggi 2-Minute Masala Noodles 70g', '8901058852300', 'NOOD-MAG-70G', 'Nestle Maggi', '1902', 12.00, 10.50, 13.00, 14.00, 20.000, 50.000),
(5, 1, 2, 2, 'Amul Butter Pasteurized 500g', '8901262010053', 'BTR-AMUL-500G', 'Amul', '0405', 12.00, 238.00, 265.00, 275.00, 5.000, 15.000),
(6, 1, 3, 2, 'Parle-G Gold Gluco Biscuits 1kg', '8901719102048', 'BISC-PARLE-1KG', 'Parle', '1905', 18.00, 95.00, 110.00, 120.00, 10.000, 25.000),
(7, 1, 1, 2, 'India Gate Basmati Rice Rozzana 5kg', '8901537005128', 'RICE-IGATE-5KG', 'India Gate', '1006', 5.00, 395.00, 460.00, 499.00, 4.000, 12.000),
(8, 1, 6, 1, 'Dettol Original Bathing Soap 125g', '8901396001017', 'SOAP-DETT-125G', 'Reckitt Dettol', '3401', 18.00, 40.00, 50.00, 55.00, 12.000, 30.000),
(9, 1, 7, 2, 'Surf Excel Quick Wash Detergent 1kg', '8901030383124', 'DET-SURF-1KG', 'Hindustan Unilever', '3402', 18.00, 114.00, 135.00, 145.00, 8.000, 20.000),
(10, 1, 4, 2, 'Brooke Bond Red Label Tea 500g', '8901030825310', 'TEA-REDLAB-500G', 'Brooke Bond', '0902', 5.00, 225.00, 260.00, 280.00, 6.000, 18.000),
(11, 1, 5, 2, 'MDH Deggi Mirch Powder 100g', '8902167000105', 'SPICE-MDH-DM100', 'MDH', '0904', 5.00, 62.00, 76.00, 82.00, 8.000, 20.000),
(12, 1, 7, 2, 'Eveready Carbon Zinc AA (Pack of 4)', '8901234005012', 'BAT-EVE-AA4', 'Eveready', '8506', 18.00, 55.00, 72.00, 80.00, 5.000, 15.000),
(13, 1, 3, 1, 'Cadbury Dairy Milk Silk Chocolate 60g', '8901233024823', 'CHOC-SILK-60G', 'Cadbury', '1806', 18.00, 66.00, 80.00, 85.00, 10.000, 25.000),
(14, 1, 1, 3, 'Loose Sona Masoori Rice Super (Per Kg)', '8901999000014', 'RICE-SONA-KG', 'Aapna Mandi', '1006', 0.00, 46.00, 58.00, 65.00, 25.000, 80.000),
(15, 1, 1, 3, 'Loose Toor Dal Unpolished (Per Kg)', '8901999000021', 'DAL-TOOR-KG', 'Aapna Mandi', '0713', 0.00, 142.00, 165.00, 180.00, 20.000, 60.000)
ON CONFLICT (id) DO NOTHING;

-- 10. Inventory Batches (FEFO queues)
INSERT INTO inventory_batches (id, store_id, product_id, batch_number, quantity, initial_quantity, cost_price, selling_price, mrp, expiry_date, status)
VALUES 
-- Atta
(1, 1, 1, 'BATCH-ATT-26A', 28.000, 30.000, 215.00, 245.00, 260.00, CURRENT_DATE + INTERVAL '120 days', 'ACTIVE'),
-- Fortune Oil
(2, 1, 2, 'BATCH-OIL-091', 45.000, 50.000, 132.00, 152.00, 165.00, CURRENT_DATE + INTERVAL '240 days', 'ACTIVE'),
-- Tata Salt
(3, 1, 3, 'BATCH-SLT-882', 60.000, 60.000, 21.00, 26.00, 28.00, CURRENT_DATE + INTERVAL '720 days', 'ACTIVE'),
-- Maggi: Batch 1 (FEFO Earliest: 18 units expiring in 15 days for alert testing!)
(4, 1, 4, 'BATCH-MAG-01A', 18.000, 48.000, 10.50, 13.00, 14.00, CURRENT_DATE + INTERVAL '15 days', 'ACTIVE'),
-- Maggi: Batch 2 (FEFO Later: 120 units expiring in 180 days)
(5, 1, 4, 'BATCH-MAG-02B', 120.000, 120.000, 10.50, 13.00, 14.00, CURRENT_DATE + INTERVAL '180 days', 'ACTIVE'),
-- Amul Butter (Expires in 45 days)
(6, 1, 5, 'BATCH-AMU-B12', 22.000, 25.000, 238.00, 265.00, 275.00, CURRENT_DATE + INTERVAL '45 days', 'ACTIVE'),
-- Parle-G
(7, 1, 6, 'BATCH-PRL-404', 35.000, 40.000, 95.00, 110.00, 120.00, CURRENT_DATE + INTERVAL '150 days', 'ACTIVE'),
-- India Gate Rice
(8, 1, 7, 'BATCH-IGR-901', 14.000, 15.000, 395.00, 460.00, 499.00, CURRENT_DATE + INTERVAL '300 days', 'ACTIVE'),
-- Dettol Soap (Low stock trigger: only 4 units left!)
(9, 1, 8, 'BATCH-DET-331', 4.000, 48.000, 40.00, 50.00, 55.00, CURRENT_DATE + INTERVAL '500 days', 'ACTIVE'),
-- Surf Excel
(10, 1, 9, 'BATCH-SRF-771', 19.000, 20.000, 114.00, 135.00, 145.00, CURRENT_DATE + INTERVAL '600 days', 'ACTIVE'),
-- Red Label Tea
(11, 1, 10, 'BATCH-TEA-552', 25.000, 30.000, 225.00, 260.00, 280.00, CURRENT_DATE + INTERVAL '360 days', 'ACTIVE'),
-- MDH Deggi Mirch
(12, 1, 11, 'BATCH-MDH-119', 30.000, 30.000, 62.00, 76.00, 82.00, CURRENT_DATE + INTERVAL '280 days', 'ACTIVE'),
-- Eveready Battery (Out of stock trigger: 0 units!)
(13, 1, 12, 'BATCH-EVE-002', 0.000, 24.000, 55.00, 72.00, 80.00, CURRENT_DATE + INTERVAL '400 days', 'DEPLETED'),
-- Dairy Milk Silk
(14, 1, 13, 'BATCH-CAD-884', 32.000, 36.000, 66.00, 80.00, 85.00, CURRENT_DATE + INTERVAL '120 days', 'ACTIVE'),
-- Loose Sona Masoori Rice (425.500 Kg in stock)
(15, 1, 14, 'BATCH-LSM-001', 425.500, 500.000, 46.00, 58.00, 65.00, CURRENT_DATE + INTERVAL '180 days', 'ACTIVE'),
-- Loose Toor Dal (85.250 Kg in stock)
(16, 1, 15, 'BATCH-LTD-001', 85.250, 100.000, 142.00, 165.00, 180.00, CURRENT_DATE + INTERVAL '180 days', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Reset all primary key sequences to avoid ID collision
SELECT setval('businesses_id_seq', (SELECT MAX(id) FROM businesses));
SELECT setval('stores_id_seq', (SELECT MAX(id) FROM stores));
SELECT setval('store_settings_id_seq', (SELECT MAX(id) FROM store_settings));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('units_id_seq', (SELECT MAX(id) FROM units));
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('suppliers_id_seq', (SELECT MAX(id) FROM suppliers));
SELECT setval('customers_id_seq', (SELECT MAX(id) FROM customers));
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
SELECT setval('inventory_batches_id_seq', (SELECT MAX(id) FROM inventory_batches));
SELECT setval('khata_transactions_id_seq', (SELECT MAX(id) FROM khata_transactions));
