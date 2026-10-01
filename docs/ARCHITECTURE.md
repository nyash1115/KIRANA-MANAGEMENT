# System Architecture & Technical Specification
## Scalable Kirana Store Management & POS System

### 1. High-Level Architecture Overview
The system is built as a high-performance, maintainable **Modular Monolith** designed specifically for Indian Kirana (retail grocery) operations. It supports fast, sub-second checkout via barcode scanners, multi-batch FEFO inventory management, GST compliance (CGST, SGST, IGST), dual-layout invoice generation (Thermal 80mm & A4), Customer Khata (Udhaar) ledger accounting, and seamless expansion from single-shop to multi-branch operations.

```mermaid
graph TD
    subgraph Client Layer
        WebPOS["React + TypeScript SPA (Desktop / Laptop POS)"]
        TabletPOS["Responsive Tablet POS"]
        Scanner["Hardware Barcode Scanner (USB/Bluetooth HID)"]
        Scanner --> WebPOS
    end

    subgraph API Gateway & Security Layer
        CorsFilter["CORS & Rate Limiter"]
        JwtFilter["JWT Authentication Filter"]
        RBAC["Role-Based Access Control (ADMIN, MANAGER, CASHIER)"]
        ExceptionHandler["Global RestControllerAdvice"]
    end

    subgraph Business Logic Layer (Spring Boot 3.3.x Modular Monolith)
        AuthModule["Authentication & User Module"]
        CatalogModule["Product, Category & Unit Catalog"]
        InventoryModule["FEFO Multi-Batch Inventory & Alerts"]
        PosModule["POS, Billing, Cart & Invoice Engine"]
        SalesModule["Sales, Returns & Cancellations"]
        PurchasesModule["Purchases, Returns & Supplier Payables"]
        KhataModule["Customer Credit (Khata) & Payments"]
        ReportsModule["Profit, COGS & Sales Analytics"]
        AuditModule["Immutable Audit Logger"]
    end

    subgraph Persistence Layer
        Flyway["Flyway Migration Engine"]
        Hibernate["Spring Data JPA / Hibernate ORM"]
        Postgres[(PostgreSQL 16/18 Relational DB)]
    end

    WebPOS --> CorsFilter
    TabletPOS --> CorsFilter
    CorsFilter --> JwtFilter
    JwtFilter --> RBAC
    RBAC --> Business Logic Layer
    Business Logic Layer --> Hibernate
    Hibernate --> Postgres
    Flyway --> Postgres
```

---

### 2. Multi-Store Hierarchy & Tenancy Model
To enable smooth transition from a standalone neighborhood Kirana to a multi-branch supermarket chain, the schema establishes a clean tenancy hierarchy:

```
Business (Tenant Root)
  └── Stores / Branches (Physical Location)
        ├── StoreSettings (GSTIN, Address, Receipt config, Thresholds)
        ├── Users (Cashiers, Managers, Store Admins)
        ├── InventoryBatches (Store-specific physical stock, FEFO queues)
        ├── Purchases & Supplier Payables
        ├── POS Sales, Invoices & Cash Registers
        ├── Customer Khata Ledgers
        └── Stock Adjustments & Notifications
```
- **Product Master Sharing**: Products, Categories, and Units belong to the `Business` level so that catalog master data is created once and reused across all branches.
- **Stock & Financial Isolation**: `InventoryBatch`, `Sale`, `Purchase`, and `KhataTransaction` are partitioned by `store_id`.

---

### 3. Inventory & FEFO (First-Expiry, First-Out) Engine
Kirana stores carry perishable FMCG goods (dairy, bread, packaged snacks, spices) alongside non-perishables. The FEFO engine ensures shelf-life optimization:
1. Every purchase creates or updates an `InventoryBatch` containing:
   - `batch_number`
   - `expiry_date`
   - `cost_price`
   - `selling_price`
   - `mrp`
   - `quantity` (BigDecimal with 3 decimal precision for items sold by weight like 1.250 Kg Rice)
2. When an item barcode is scanned at the POS:
   ```sql
   SELECT b FROM InventoryBatch b 
   WHERE b.product.id = :productId 
     AND b.store.id = :storeId 
     AND b.quantity > 0 
     AND (b.expiryDate IS NULL OR b.expiryDate >= CURRENT_DATE)
   ORDER BY b.expiryDate ASC NULLS LAST, b.createdAt ASC
   ```
3. **Pessimistic Concurrency**: If multiple cashiers ring up the same batch simultaneously, inventory deductions acquire a `PESSIMISTIC_WRITE` row lock on `inventory_batches` to prevent overselling and negative stock.
4. **Zero-Waste Allocation**: If a customer purchases 5 units and Batch A has only 3 units expiring in 5 days, the POS automatically consumes 3 from Batch A and 2 from Batch B (expiring in 20 days), recording individual item cost bases for exact COGS calculation.

---

### 4. Financial & GST Calculation Specifications
Financial integrity is enforced strictly in the backend business layer:

#### Tax & Totals Breakdown
- **Item Level Calculation**:
  - `Gross Item Total = Selling Price × Quantity`
  - `Item Discount Amount = Item Discount`
  - `Item Net Taxable = Gross Item Total - Item Discount`
  - `CGST Amount = Item Net Taxable × (GST Rate / 2) / 100` (for Intra-State)
  - `SGST Amount = Item Net Taxable × (GST Rate / 2) / 100` (for Intra-State)
  - `IGST Amount = Item Net Taxable × GST Rate / 100` (for Inter-State)
  - `Line Total = Item Net Taxable + CGST + SGST + IGST`
- **Bill Level Summary**:
  - `Subtotal = Σ(Gross Item Totals)`
  - `Total Item Discounts = Σ(Item Discounts)`
  - `Bill Discount Amount = (Subtotal - Total Item Discounts) × Bill Discount %`
  - `Taxable Amount = Subtotal - Total Item Discounts - Bill Discount Amount`
  - `CGST Total = Σ(CGST Amount)`
  - `SGST Total = Σ(SGST Amount)`
  - `Total Tax = CGST Total + SGST Total + IGST Total`
  - `Raw Grand Total = Taxable Amount + Total Tax`
  - `Round Off = Math.round(Raw Grand Total) - Raw Grand Total` (e.g. ₹104.40 rounds to ₹104.00, roundOff = -0.40)
  - `Grand Total = Raw Grand Total + Round Off`

#### COGS and Gross Profit Calculation
Purchases increase stock asset value, not immediate expense. Profit is recognized at the moment of sale:
$$\text{Line COGS} = \text{Batch Cost Price} \times \text{Quantity}$$
$$\text{Total COGS} = \sum (\text{Line COGS})$$
$$\text{Net Sales} = \text{Taxable Amount}$$
$$\text{Gross Profit} = \text{Net Sales} - \text{Total COGS}$$
$$\text{Gross Margin \%} = \left(\frac{\text{Gross Profit}}{\text{Net Sales}}\right) \times 100$$

---

### 5. Khata (Udhaar) Ledger Integrity
Kirana stores operate heavily on local customer trust and credit. The Khata module guarantees double-entry balance consistency:
1. `current_outstanding` on `customers` represents total unpaid credit.
2. When a sale is completed with `PaymentMethod = UDHAAR` (or partial Udhaar in split payments):
   - A `KhataTransaction` of type `DEBIT_SALE` is created.
   - `customer.current_outstanding += credit_amount`
3. When the customer pays:
   - A `CustomerPayment` record is created (Cash/UPI/Bank).
   - A `KhataTransaction` of type `CREDIT_PAYMENT` is created.
   - `customer.current_outstanding -= payment_amount`
4. On Sales Return with refund method `KHATA_CREDIT`:
   - A `KhataTransaction` of type `RETURN_CREDIT` is created.
   - `customer.current_outstanding -= refund_amount`
5. The ledger constraint:
   $$\text{Current Outstanding} = \sum (\text{DEBIT}) - \sum (\text{CREDIT}) \pm \text{ADJUSTMENTS}$$

---

### 6. Role-Based Access Control (RBAC)
| Capability | ADMIN / OWNER | MANAGER | CASHIER |
|---|:---:|:---:|:---:|
| POS Billing & Checkout | Yes | Yes | Yes |
| Barcode Scan & Cart Management | Yes | Yes | Yes |
| Print Invoices | Yes | Yes | Yes |
| Customer Khata View | Yes | Yes | Read Only |
| Record Khata Repayment | Yes | Yes | Yes |
| Product & Price Editing | Yes | Yes | No |
| View Cost Price & Gross Profit | Yes | No | No |
| Financial & Margin Reports | Yes | No | No |
| Purchases & Supplier Management | Yes | Yes | No |
| Manual Stock Adjustments | Yes | Yes | No |
| Invoice Cancellation | Yes | Yes (Configurable) | No |
| User & Role Management | Yes | No | No |
| Audit Log Inspection | Yes | No | No |
| Store & Tax Settings | Yes | No | No |

---

### 7. Performance & Scalability Tactics
- **Barcode Lookup Sub-50ms**: Indexed B-Tree search on `products.barcode` and `inventory_batches.product_id`.
- **Server-Side Pagination**: Every list endpoint uses `Pageable` with configurable sort and filter criteria.
- **DTO Projection**: Prevents JPA N+1 query overhead by joining batch and product details in focused query contracts.
- **Transactional Rollback**: ACID transactions wrap `@Transactional(rollbackFor = Exception.class)` across Sale + SaleItems + Batch Deductions + Payment + Khata + Audit records.
