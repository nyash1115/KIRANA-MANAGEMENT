# REST API Specification (OpenAPI Compatible)
## Scalable Kirana Store Management & POS System

### 1. Standard API Formats

#### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-09-30T12:00:00Z"
}
```

#### Paginated Response
```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 154,
    "totalPages": 8,
    "last": false
  },
  "timestamp": "2026-09-30T12:00:00Z"
}
```

#### Error Response
```json
{
  "success": false,
  "message": "Product with barcode 8901234567890 not found",
  "errorCode": "RESOURCE_NOT_FOUND",
  "errors": null,
  "timestamp": "2026-09-30T12:00:00Z"
}
```

---

### 2. Core API Endpoints

#### Authentication & Profile (`/api/v1/auth`)
- `POST /api/v1/auth/login`: Authenticate with username and password, returns JWT token, user profile, role, and store ID.
- `GET /api/v1/auth/me`: Retrieve current logged-in user details and permissions.
- `POST /api/v1/auth/change-password`: Update account password.

#### Products & Catalog (`/api/v1/products`)
- `GET /api/v1/products`: Search and filter products (supports `search`, `categoryId`, `brand`, `status`, pagination).
- `POST /api/v1/products`: Create a new product.
- `GET /api/v1/products/{id}`: Fetch product details with active batches.
- `PUT /api/v1/products/{id}`: Update product fields.
- `DELETE /api/v1/products/{id}`: Soft delete/deactivate product.
- `GET /api/v1/products/barcode/{barcode}`: Fast POS scan lookup returning product + optimal FEFO batch.
- `GET /api/v1/categories`: List active categories.
- `POST /api/v1/categories`: Create category.
- `PUT /api/v1/categories/{id}`: Update category.
- `DELETE /api/v1/categories/{id}`: Deactivate category.
- `GET /api/v1/units`: List standard units (Kg, Gram, Piece, etc.).

#### Inventory & Batches (`/api/v1/inventory`)
- `GET /api/v1/inventory/batches`: Query batches by store with search, expiry filters, status.
- `GET /api/v1/inventory/low-stock`: Get items at or below reorder level.
- `GET /api/v1/inventory/expiring`: Get batches expiring within threshold days.
- `GET /api/v1/inventory/expired`: Get expired inventory batches.
- `POST /api/v1/inventory/adjust`: Record manual stock adjustments (damaged, lost, counting error).
- `GET /api/v1/inventory/adjustments`: Query stock adjustment audit records.

#### Purchases & Suppliers (`/api/v1/purchases`, `/api/v1/suppliers`)
- `GET /api/v1/suppliers`: Search and list suppliers with balances.
- `POST /api/v1/suppliers`: Create supplier.
- `PUT /api/v1/suppliers/{id}`: Update supplier.
- `POST /api/v1/purchases`: Record new inward stock purchase from supplier (creates/updates inventory batches).
- `GET /api/v1/purchases`: List purchase invoices with date/supplier filters.
- `GET /api/v1/purchases/{id}`: Get purchase details with line items.
- `POST /api/v1/purchases/{id}/return`: Process purchase return to vendor.

#### POS & Sales (`/api/v1/sales`)
- `POST /api/v1/sales/calculate`: Real-time cart calculation (validates items, computes GST, item discounts, bill discounts, round-off, totals without persisting).
- `POST /api/v1/sales`: Execute checkout (atomically creates sale, deducts FEFO inventory, records split payments, updates Khata if credit, creates audit log).
- `GET /api/v1/sales`: Filter sales history by date range, invoice number, customer, cashier, payment method.
- `GET /api/v1/sales/{id}`: Retrieve sale invoice details with line items and payment breakdown.
- `GET /api/v1/sales/{id}/invoice`: Generate print-ready invoice data (supports Thermal 80mm & A4 formats).
- `POST /api/v1/sales/{id}/cancel`: Cancel bill with reason, reversing inventory and finances.
- `POST /api/v1/sales/{id}/return`: Customer sales return with items, restocking batch stock, adjusting profit, and issuing refund or Khata credit.

#### Customers & Khata Udhaar (`/api/v1/customers`)
- `GET /api/v1/customers`: Search customers by name or mobile number.
- `POST /api/v1/customers`: Create customer profile.
- `PUT /api/v1/customers/{id}`: Update customer.
- `GET /api/v1/customers/{id}/khata`: Retrieve customer Khata ledger statement (all debit sales, repayments, running balance).
- `POST /api/v1/customers/{id}/payments`: Record customer credit repayment (Cash/UPI), reducing outstanding balance.

#### Dashboard & Reports (`/api/v1/dashboard`, `/api/v1/reports`)
- `GET /api/v1/dashboard/summary`: Today's sales, bills count, gross profit, stock value, pending Khata dues.
- `GET /api/v1/dashboard/charts`: Sales trends, top selling items, category split, payment breakdown (Today, Last 7 Days, Month, Custom).
- `GET /api/v1/reports/sales`: Detailed sales summary report with CSV/Excel export.
- `GET /api/v1/reports/profit`: Profit & Loss statement (Net Sales, COGS, Gross Profit, Gross Margin %).
- `GET /api/v1/reports/inventory-valuation`: Total stock value at cost and selling price.
- `GET /api/v1/reports/gst-summary`: GSTR-1 preparation report (Taxable turnover, CGST, SGST, IGST by HSN/tax slab).

#### Notifications & Alerts (`/api/v1/notifications`)
- `GET /api/v1/notifications`: List operational alerts (Low stock, Expired, Payment dues).
- `POST /api/v1/notifications/{id}/read`: Mark notification as read.
- `POST /api/v1/notifications/read-all`: Mark all alerts as read.

#### Store Settings & Audit (`/api/v1/settings`, `/api/v1/audit`)
- `GET /api/v1/settings`: Fetch store configuration (tax inclusive mode, invoice prefix, paper size, currency, thresholds).
- `PUT /api/v1/settings`: Update store configuration.
- `GET /api/v1/audit`: Query system audit trail logs (Admins only).
