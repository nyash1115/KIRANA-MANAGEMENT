export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  errors?: string[];
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface User {
  id: number;
  username: string;
  fullName: string;
  role: 'ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_CASHIER';
  storeId?: number;
  businessId?: number;
  token?: string;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
  active: boolean;
}

export interface Unit {
  id: number;
  name: string;
  code: string;
  allowDecimals: boolean;
}

export interface Product {
  id: number;
  categoryId: number;
  categoryName: string;
  unitId: number;
  unitName: string;
  unitCode: string;
  allowDecimals: boolean;
  name: string;
  barcode?: string;
  sku: string;
  brand?: string;
  hsnCode?: string;
  gstRate: number;
  defaultCostPrice: number;
  defaultSellingPrice: number;
  defaultMrp: number;
  minStockLevel: number;
  reorderLevel: number;
  totalAvailableStock?: number;
  imageUrl?: string;
  description?: string;
  active: boolean;
  createdAt?: string;
}

export interface ProductRequest {
  categoryId: number;
  unitId: number;
  name: string;
  barcode?: string;
  sku: string;
  brand?: string;
  hsnCode?: string;
  gstRate: number;
  defaultCostPrice: number;
  defaultSellingPrice: number;
  defaultMrp: number;
  minStockLevel: number;
  reorderLevel: number;
  imageUrl?: string;
  description?: string;
  active: boolean;
}

export interface InventoryBatch {
  id: number;
  storeId: number;
  productId: number;
  productName: string;
  productBarcode?: string;
  productSku: string;
  unitName: string;
  unitCode: string;
  batchNumber: string;
  quantity: number;
  initialQuantity: number;
  costPrice: number;
  sellingPrice: number;
  mrp: number;
  expiryDate?: string;
  status: 'ACTIVE' | 'DEPLETED' | 'EXPIRED';
  expired?: boolean;
  nearExpiry?: boolean;
  createdAt: string;
}

export interface StockAdjustmentRequest {
  storeId: number;
  productId: number;
  batchId?: number;
  quantityDelta: number;
  reason: 'DAMAGED' | 'EXPIRED' | 'THEFT_OR_LOST' | 'COUNTING_ERROR' | 'MANUAL_CORRECTION';
  notes?: string;
}

export interface Supplier {
  id: number;
  name: string;
  contactPerson?: string;
  phone: string;
  email?: string;
  address?: string;
  gstin?: string;
  outstandingBalance: number;
  notes?: string;
  active: boolean;
  createdAt?: string;
}

export interface SupplierRequest {
  name: string;
  contactPerson?: string;
  phone: string;
  email?: string;
  address?: string;
  gstin?: string;
  notes?: string;
}

export interface Customer {
  id: number;
  name: string;
  phone: string;
  email?: string;
  address?: string;
  gstin?: string;
  creditLimit: number;
  outstandingBalance: number;
  currentOutstanding?: number;
  notes?: string;
  active: boolean;
  createdAt?: string;
}

export interface CustomerRequest {
  name: string;
  phone: string;
  email?: string;
  address?: string;
  gstin?: string;
  creditLimit?: number;
  notes?: string;
  active?: boolean;
}

export interface CustomerPaymentRequest {
  storeId: number;
  amount: number;
  paymentMethod: 'CASH' | 'UPI' | 'BANK_TRANSFER';
  transactionReference?: string;
  notes?: string;
}

export interface PaymentItem {
  paymentMethod: 'CASH' | 'UPI' | 'CARD' | 'UDHAAR';
  amount: number;
  transactionRef?: string;
  notes?: string;
}

export interface PaymentRequest {
  paymentMethod: 'CASH' | 'UPI' | 'CARD' | 'UDHAAR';
  amount: number;
  transactionReference?: string;
  notes?: string;
}

export interface SaleItem {
  id: number;
  productId: number;
  productName: string;
  productBarcode?: string;
  productSku: string;
  unitName: string;
  batchId?: number;
  batchNumber?: string;
  quantity: number;
  unitPrice: number;
  costPrice: number;
  discountAmount: number;
  taxableAmount: number;
  gstRate: number;
  cgstAmount: number;
  sgstAmount: number;
  igstAmount: number;
  totalTax: number;
  lineTotal: number;
  lineProfit: number;
  returnedQuantity: number;
}

export interface Sale {
  id: number;
  storeId: number;
  storeName: string;
  storeCode?: string;
  storeAddress: string;
  storeCity?: string;
  storeState?: string;
  storePincode?: string;
  storePhone: string;
  storeGstin?: string;
  invoiceFooterMessage?: string;
  invoiceTerms?: string;
  thermalPaperWidthMm?: number;

  customerId?: number;
  customerName?: string;
  customerPhone?: string;
  customerCurrentOutstanding?: number;

  cashierName: string;
  invoiceNumber: string;
  saleDate: string;
  createdAt: string;

  subtotal: number;
  itemDiscountTotal: number;
  billDiscountRate?: number;
  billDiscountAmount: number;
  taxableAmount: number;
  cgstAmount: number;
  sgstAmount: number;
  igstAmount: number;
  totalTaxAmount: number;
  roundOff: number;
  totalAmount: number;
  grandTotal: number;
  totalCogs: number;
  grossProfit: number;
  paidAmount: number;

  status: 'COMPLETED' | 'CANCELLED' | 'PARTIALLY_RETURNED' | 'FULLY_RETURNED' | 'RETURNED';
  paymentStatus: 'PAID' | 'PARTIAL' | 'CREDIT';
  notes?: string;
  cancelReason?: string;

  items: SaleItem[];
  payments: PaymentItem[];
}

export interface SaleReturnItemRequest {
  saleItemId: number;
  quantity: number;
  reason: string;
  restockInventory: boolean;
}

export interface SaleReturnRequest {
  storeId: number;
  items: SaleReturnItemRequest[];
  refundMethod: 'CASH' | 'UPI' | 'KHATA_CREDIT';
  notes?: string;
}

export interface PurchaseItemRequest {
  productId: number;
  quantity: number;
  costPrice: number;
  sellingPrice?: number;
  mrp?: number;
  batchNumber: string;
  expiryDate?: string;
  gstRate: number;
}

export interface PurchaseRequest {
  storeId: number;
  supplierId: number;
  supplierInvoiceNumber?: string;
  purchaseDate: string;
  items: PurchaseItemRequest[];
  paymentStatus: 'PAID' | 'PARTIAL' | 'UNPAID';
  amountPaid: number;
  notes?: string;
}

export interface PurchaseItem {
  id?: number;
  productId: number;
  productName?: string;
  productBarcode?: string;
  batchNumber: string;
  expiryDate?: string;
  quantity: number;
  costPrice: number;
  sellingPrice?: number;
  mrp?: number;
  gstRate: number;
  gstAmount: number;
  totalAmount: number;
}

export interface Purchase {
  id: number;
  storeId: number;
  supplierId: number;
  supplierName: string;
  supplierPhone: string;
  purchaseNumber: string;
  invoiceNumber?: string;
  supplierInvoiceNumber?: string;
  purchaseDate: string;
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  amountPaid: number;
  paidAmount?: number;
  paymentStatus: 'PAID' | 'PARTIAL' | 'UNPAID';
  paymentMethod?: string;
  status: string;
  notes?: string;
  createdByName?: string;
  createdAt: string;
  items: PurchaseItem[];
}

export interface KhataTransaction {
  id: number;
  customerId: number;
  customerName?: string;
  customerPhone?: string;
  transactionType: 'DEBIT' | 'CREDIT' | 'DEBIT_SALE' | 'CREDIT_PAYMENT' | 'ADJUSTMENT' | 'RETURN_CREDIT';
  type?: string;
  referenceType?: string;
  referenceId?: string;
  referenceNumber?: string;
  amount: number;
  runningBalance: number;
  balanceAfter?: number;
  notes?: string;
  createdByName?: string;
  createdAt: string;
  transactionDate?: string;
}

export interface DashboardSummary {
  todaySales: number;
  todayBillsCount: number;
  todayGrossProfit: number;
  totalInventoryValue: number;
  totalInventoryValueCost?: number;
  totalInventoryValueSelling?: number;
  lowStockCount: number;
  outOfStockCount: number;
  nearExpiryCount: number;
  expiredCount: number;
  pendingCustomerPayments: number;
  pendingCustomerKhataDues?: number;
  pendingSupplierPayables: number;
  salesTrend: { date: string; bills?: number; sales: number; profit: number }[];
  topSellingProducts: { productName: string; quantitySold: number; revenue: number }[];
  categorySales: { categoryName: string; totalSales: number }[];
}

export interface StoreSettings {
  id: number;
  storeId: number;
  storeName?: string;
  storeAddress?: string;
  storePhone?: string;
  storeGstin?: string;
  invoicePrefix: string;
  defaultGstType: 'EXCLUSIVE' | 'INCLUSIVE';
  defaultGstRate?: number;
  enableNegativeStock: boolean;
  allowNegativeStock?: boolean;
  lowStockThreshold: number;
  expiryWarningDays: number;
  receiptFooterMessage: string;
  termsAndConditions: string;
  thermalPaperWidthMm?: number;
  currencySymbol?: string;
  currencyCode?: string;
}

export interface Notification {
  id: number;
  storeId?: number;
  type: 'LOW_STOCK' | 'OUT_OF_STOCK' | 'NEAR_EXPIRY' | 'EXPIRED' | 'CUSTOMER_PAYMENT_DUE' | 'SUPPLIER_PAYMENT_DUE' | string;
  title: string;
  message: string;
  severity?: 'INFO' | 'WARNING' | 'CRITICAL';
  read?: boolean;
  isRead?: boolean;
  createdAt: string;
}

export type NotificationItem = Notification;

export interface AuditLog {
  id: number;
  businessId?: number;
  storeId?: number;
  userId?: number;
  username: string;
  action: string;
  entityName: string;
  entityId: string;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  createdAt: string;
}

export type AuditLogItem = AuditLog;

export interface ProfitReport {
  grossSales: number;
  totalDiscounts: number;
  netSales: number;
  costOfGoodsSold: number;
  grossProfit: number;
  profitMarginPercentage: number;
}

export interface GstSlabSummary {
  rate: number;
  taxableAmount: number;
  cgst: number;
  sgst: number;
  igst: number;
  totalTax: number;
}

export interface GstSummaryReport {
  startDate: string;
  endDate: string;
  totalTaxableAmount: number;
  totalCgst: number;
  totalSgst: number;
  totalIgst: number;
  totalTaxAmount: number;
  slabs: GstSlabSummary[];
}
