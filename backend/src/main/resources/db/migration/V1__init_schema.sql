-- V1__init_schema.sql: Scalable Kirana Store Relational Schema

-- 1. Businesses
CREATE TABLE businesses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    trade_name VARCHAR(150),
    tax_number VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Stores
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    gstin VARCHAR(15),
    invoice_prefix VARCHAR(10) NOT NULL DEFAULT 'INV',
    invoice_next_seq BIGINT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Store Settings
CREATE TABLE store_settings (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL UNIQUE REFERENCES stores(id) ON DELETE CASCADE,
    tax_inclusive_pricing BOOLEAN NOT NULL DEFAULT TRUE,
    enable_igst BOOLEAN NOT NULL DEFAULT FALSE,
    allow_negative_stock BOOLEAN NOT NULL DEFAULT FALSE,
    default_gst_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    low_stock_threshold_default NUMERIC(12,3) NOT NULL DEFAULT 5.000,
    expiry_alert_days_default INT NOT NULL DEFAULT 30,
    invoice_footer_message TEXT DEFAULT 'Thank you for shopping with us! Visit again.',
    invoice_terms TEXT DEFAULT 'Goods once sold will only be exchanged within 3 days with bill.',
    thermal_paper_width_mm INT NOT NULL DEFAULT 80,
    currency_symbol VARCHAR(10) NOT NULL DEFAULT '₹',
    currency_code VARCHAR(10) NOT NULL DEFAULT 'INR',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    store_id BIGINT REFERENCES stores(id) ON DELETE SET NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL, -- ROLE_ADMIN, ROLE_MANAGER, ROLE_CASHIER
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. Categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_categories_business_slug UNIQUE (business_id, slug)
);

-- 6. Units
CREATE TABLE units (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    allow_decimals BOOLEAN NOT NULL DEFAULT FALSE
);

-- 7. Products (Master Catalog)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    unit_id BIGINT NOT NULL REFERENCES units(id),
    name VARCHAR(200) NOT NULL,
    barcode VARCHAR(100) UNIQUE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    brand VARCHAR(100),
    hsn_code VARCHAR(20),
    gst_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    default_cost_price NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    default_selling_price NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    default_mrp NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    min_stock_level NUMERIC(12,3) NOT NULL DEFAULT 5.000,
    reorder_level NUMERIC(12,3) NOT NULL DEFAULT 10.000,
    image_url VARCHAR(500),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. Inventory Batches (FEFO Store Stock)
CREATE TABLE inventory_batches (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    batch_number VARCHAR(100) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL CHECK (quantity >= 0),
    initial_quantity NUMERIC(12,3) NOT NULL,
    cost_price NUMERIC(12,2) NOT NULL,
    selling_price NUMERIC(12,2) NOT NULL,
    mrp NUMERIC(12,2) NOT NULL,
    expiry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DEPLETED, EXPIRED
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9. Suppliers
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    gstin VARCHAR(15),
    outstanding_balance NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. Purchases
CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    invoice_number VARCHAR(100) NOT NULL,
    purchase_date DATE NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(12,2) NOT NULL,
    paid_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID', -- PAID, PARTIAL, UNPAID
    payment_method VARCHAR(30) DEFAULT 'BANK_TRANSFER',
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 11. Purchase Items
CREATE TABLE purchase_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    inventory_batch_id BIGINT REFERENCES inventory_batches(id),
    batch_number VARCHAR(100) NOT NULL,
    expiry_date DATE,
    quantity NUMERIC(12,3) NOT NULL,
    cost_price NUMERIC(12,2) NOT NULL,
    gst_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    gst_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(12,2) NOT NULL
);

-- 12. Customers
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    gstin VARCHAR(15),
    credit_limit NUMERIC(12,2) NOT NULL DEFAULT 5000.00,
    current_outstanding NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 13. Sales (POS Billing Header)
CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    customer_id BIGINT REFERENCES customers(id) ON DELETE SET NULL,
    cashier_id BIGINT REFERENCES users(id),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    sale_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    subtotal NUMERIC(12,2) NOT NULL,
    item_discount_total NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    bill_discount_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    bill_discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    taxable_amount NUMERIC(12,2) NOT NULL,
    cgst_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    sgst_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    igst_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    round_off NUMERIC(6,2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(12,2) NOT NULL,
    total_cogs NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    gross_profit NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    paid_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED', -- COMPLETED, CANCELLED, PARTIALLY_RETURNED, FULLY_RETURNED
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID', -- PAID, PARTIAL, CREDIT
    cancel_reason TEXT,
    cancelled_at TIMESTAMPTZ,
    cancelled_by BIGINT REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 14. Sale Items
CREATE TABLE sale_items (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    inventory_batch_id BIGINT REFERENCES inventory_batches(id),
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    cost_price NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    taxable_amount NUMERIC(12,2) NOT NULL,
    gst_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    cgst_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    sgst_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    igst_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_tax NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    line_total NUMERIC(12,2) NOT NULL,
    line_cogs NUMERIC(12,2) NOT NULL,
    line_profit NUMERIC(12,2) NOT NULL,
    returned_quantity NUMERIC(12,3) NOT NULL DEFAULT 0.000
);

-- 15. Sale Payments (Split Payment Support)
CREATE TABLE sale_payments (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    payment_method VARCHAR(30) NOT NULL, -- CASH, UPI, CARD, UDHAAR
    amount NUMERIC(12,2) NOT NULL,
    transaction_ref VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 16. Sale Returns
CREATE TABLE sale_returns (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    sale_id BIGINT NOT NULL REFERENCES sales(id),
    customer_id BIGINT REFERENCES customers(id),
    return_number VARCHAR(50) NOT NULL UNIQUE,
    return_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    subtotal_refund NUMERIC(12,2) NOT NULL,
    tax_refund NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_refund NUMERIC(12,2) NOT NULL,
    cogs_reversal NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    profit_reversal NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    refund_method VARCHAR(30) NOT NULL, -- CASH, UPI, KHATA_CREDIT
    reason TEXT NOT NULL,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 17. Sale Return Items
CREATE TABLE sale_return_items (
    id BIGSERIAL PRIMARY KEY,
    sale_return_id BIGINT NOT NULL REFERENCES sale_returns(id) ON DELETE CASCADE,
    sale_item_id BIGINT NOT NULL REFERENCES sale_items(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    inventory_batch_id BIGINT REFERENCES inventory_batches(id),
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    cost_price NUMERIC(12,2) NOT NULL,
    tax_refund NUMERIC(12,2) NOT NULL,
    total_refund NUMERIC(12,2) NOT NULL
);

-- 18. Khata (Udhaar) Ledger Transactions
CREATE TABLE khata_transactions (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL, -- DEBIT_SALE, CREDIT_PAYMENT, ADJUSTMENT, RETURN_CREDIT
    reference_type VARCHAR(30) NOT NULL, -- SALE, CUSTOMER_PAYMENT, SALE_RETURN, MANUAL
    reference_id VARCHAR(64),
    amount NUMERIC(12,2) NOT NULL,
    balance_after NUMERIC(12,2) NOT NULL,
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    transaction_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 19. Customer Payments (Udhaar Settlements)
CREATE TABLE customer_payments (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    amount NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL, -- CASH, UPI, CARD, BANK_TRANSFER
    reference_number VARCHAR(100),
    notes TEXT,
    payment_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 20. Stock Adjustments (Damage, Count, Theft)
CREATE TABLE stock_adjustments (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    inventory_batch_id BIGINT NOT NULL REFERENCES inventory_batches(id),
    adjustment_type VARCHAR(40) NOT NULL, -- DAMAGED, EXPIRED, LOST, THEFT, COUNTING_ERROR, MANUAL_CORRECTION
    quantity_before NUMERIC(12,3) NOT NULL,
    adjusted_quantity NUMERIC(12,3) NOT NULL, -- Signed value: e.g. -2.000 or +3.000
    quantity_after NUMERIC(12,3) NOT NULL,
    cost_impact NUMERIC(12,2) NOT NULL,
    reason TEXT NOT NULL,
    adjusted_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 21. Audit Logs
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    store_id BIGINT REFERENCES stores(id) ON DELETE SET NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, PRICE_CHANGE, BILL_CANCEL, RETURN, ADJUST, LOGIN
    entity_name VARCHAR(50) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 22. Notifications & Alerts
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    type VARCHAR(40) NOT NULL, -- LOW_STOCK, OUT_OF_STOCK, NEAR_EXPIRY, EXPIRED, CUSTOMER_PAYMENT_DUE, SUPPLIER_PAYMENT_DUE
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO', -- INFO, WARNING, CRITICAL
    reference_type VARCHAR(50),
    reference_id VARCHAR(64),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PERFORMANCE INDEXES
CREATE INDEX idx_products_barcode ON products(barcode) WHERE barcode IS NOT NULL;
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_business ON products(business_id);
CREATE INDEX idx_products_category ON products(category_id);

CREATE INDEX idx_batches_fefo ON inventory_batches(store_id, product_id, status, expiry_date);

CREATE INDEX idx_sales_invoice ON sales(invoice_number);
CREATE INDEX idx_sales_store_date ON sales(store_id, sale_date DESC);
CREATE INDEX idx_sales_customer ON sales(customer_id);

CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_khata_customer ON khata_transactions(customer_id, transaction_date DESC);
CREATE INDEX idx_suppliers_phone ON suppliers(phone);

CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
CREATE INDEX idx_notifications_store ON notifications(store_id, is_read, created_at DESC);
