import axios from 'axios';
import {
  ApiResponse,
  PagedResponse,
  Product,
  ProductRequest,
  Category,
  Unit,
  InventoryBatch,
  StockAdjustmentRequest,
  Supplier,
  SupplierRequest,
  Customer,
  CustomerRequest,
  CustomerPaymentRequest,
  Sale,
  SaleReturnRequest,
  Purchase,
  PurchaseRequest,
  KhataTransaction,
  DashboardSummary,
  StoreSettings,
  Notification,
  AuditLog,
  ProfitReport,
  GstSummaryReport,
} from '../types';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach JWT bearer token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('kirana_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401 unauthorized
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('kirana_token');
      localStorage.removeItem('kirana_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// =================== AUTH SERVICE ===================
export const authService = {
  login: async (credentials: any) => {
    const res = await api.post<ApiResponse<any>>('/auth/login', credentials);
    return res.data;
  },
};

// =================== PRODUCT SERVICE ===================
export const productService = {
  getAll: async (params?: { page?: number; size?: number; search?: string; categoryId?: number }) => {
    const res = await api.get<ApiResponse<PagedResponse<Product>>>('/products', { params });
    return res.data;
  },
  getById: async (id: number) => {
    const res = await api.get<ApiResponse<Product>>(`/products/${id}`);
    return res.data;
  },
  getByBarcode: async (barcode: string) => {
    const res = await api.get<ApiResponse<Product>>(`/products/barcode/${barcode}`);
    return res.data;
  },
  create: async (data: ProductRequest) => {
    const res = await api.post<ApiResponse<Product>>('/products', data);
    return res.data;
  },
  update: async (id: number, data: ProductRequest) => {
    const res = await api.put<ApiResponse<Product>>(`/products/${id}`, data);
    return res.data;
  },
  delete: async (id: number) => {
    const res = await api.delete<ApiResponse<void>>(`/products/${id}`);
    return res.data;
  },
};

// =================== CATEGORY SERVICE ===================
export const categoryService = {
  getAll: async () => {
    const res = await api.get<ApiResponse<Category[]>>('/categories');
    return res.data;
  },
  create: async (data: { name: string; description?: string }) => {
    const res = await api.post<ApiResponse<Category>>('/categories', data);
    return res.data;
  },
};

// =================== UNIT SERVICE ===================
export const unitService = {
  getAll: async () => {
    const res = await api.get<ApiResponse<Unit[]>>('/units');
    return res.data;
  },
};

// =================== INVENTORY SERVICE ===================
export const inventoryService = {
  getBatches: async (params?: { page?: number; size?: number; status?: string; productId?: number }) => {
    const res = await api.get<ApiResponse<PagedResponse<InventoryBatch>>>('/inventory/batches', { params });
    return res.data;
  },
  getLowStock: async (storeId?: number) => {
    const res = await api.get<ApiResponse<Product[]>>('/inventory/low-stock', { params: { storeId } });
    return res.data;
  },
  getExpiring: async (days?: number) => {
    const res = await api.get<ApiResponse<InventoryBatch[]>>('/inventory/expiring', { params: { days } });
    return res.data;
  },
  adjustStock: async (data: StockAdjustmentRequest) => {
    const res = await api.post<ApiResponse<void>>('/inventory/adjust', data);
    return res.data;
  },
};

// =================== SUPPLIER SERVICE ===================
export const supplierService = {
  getAll: async (params?: { page?: number; size?: number; search?: string }) => {
    const res = await api.get<ApiResponse<PagedResponse<Supplier>>>('/suppliers', { params });
    return res.data;
  },
  getById: async (id: number) => {
    const res = await api.get<ApiResponse<Supplier>>(`/suppliers/${id}`);
    return res.data;
  },
  create: async (data: SupplierRequest) => {
    const res = await api.post<ApiResponse<Supplier>>('/suppliers', data);
    return res.data;
  },
  update: async (id: number, data: SupplierRequest) => {
    const res = await api.put<ApiResponse<Supplier>>(`/suppliers/${id}`, data);
    return res.data;
  },
};

// =================== PURCHASE SERVICE ===================
export const purchaseService = {
  getAll: async (params?: { page?: number; size?: number; supplierId?: number }) => {
    const res = await api.get<ApiResponse<PagedResponse<Purchase>>>('/purchases', { params });
    return res.data;
  },
  getById: async (id: number) => {
    const res = await api.get<ApiResponse<Purchase>>(`/purchases/${id}`);
    return res.data;
  },
  create: async (data: PurchaseRequest) => {
    const res = await api.post<ApiResponse<Purchase>>('/purchases', data);
    return res.data;
  },
};

// =================== CUSTOMER SERVICE ===================
export const customerService = {
  getAll: async (params?: { page?: number; size?: number; search?: string }) => {
    const res = await api.get<ApiResponse<PagedResponse<Customer>>>('/customers', { params });
    return res.data;
  },
  getById: async (id: number) => {
    const res = await api.get<ApiResponse<Customer>>(`/customers/${id}`);
    return res.data;
  },
  create: async (data: CustomerRequest) => {
    const res = await api.post<ApiResponse<Customer>>('/customers', data);
    return res.data;
  },
  update: async (id: number, data: CustomerRequest) => {
    const res = await api.put<ApiResponse<Customer>>(`/customers/${id}`, data);
    return res.data;
  },
  recordPayment: async (customerId: number, data: CustomerPaymentRequest) => {
    const res = await api.post<ApiResponse<void>>(`/customers/${customerId}/payments`, data);
    return res.data;
  },
  getKhata: async (customerId: number) => {
    const res = await api.get<ApiResponse<KhataTransaction[]>>(`/customers/${customerId}/khata`);
    return res.data;
  },
};

// =================== SALE SERVICE ===================
export const saleService = {
  getAll: async (params?: { page?: number; size?: number; invoiceNumber?: string; status?: string }) => {
    const res = await api.get<ApiResponse<PagedResponse<Sale>>>('/sales', { params });
    return res.data;
  },
  getById: async (id: number) => {
    const res = await api.get<ApiResponse<Sale>>(`/sales/${id}`);
    return res.data;
  },
  getByInvoiceNumber: async (invNo: string) => {
    const res = await api.get<ApiResponse<Sale>>(`/sales/invoice/${invNo}`);
    return res.data;
  },
  create: async (data: any) => {
    const res = await api.post<ApiResponse<Sale>>('/sales', data);
    return res.data;
  },
  processReturn: async (saleId: number, data: SaleReturnRequest) => {
    const res = await api.post<ApiResponse<any>>(`/sales/${saleId}/return`, data);
    return res.data;
  },
  cancelSale: async (saleId: number, reason: string) => {
    const res = await api.post<ApiResponse<void>>(`/sales/${saleId}/cancel`, { reason });
    return res.data;
  },
};

// =================== DASHBOARD SERVICE ===================
export const dashboardService = {
  getSummary: async () => {
    const res = await api.get<ApiResponse<DashboardSummary>>('/dashboard/summary');
    return res.data;
  },
  getCharts: async (period: string = 'week') => {
    const res = await api.get<ApiResponse<any>>('/dashboard/charts', { params: { period } });
    return res.data;
  },
};

// =================== REPORT SERVICE ===================
export const reportService = {
  getProfitReport: async (startDate?: string, endDate?: string) => {
    const res = await api.get<ApiResponse<ProfitReport>>('/reports/profit', {
      params: { startDate, endDate },
    });
    return res.data;
  },
  getGstReport: async (startDate?: string, endDate?: string) => {
    const res = await api.get<ApiResponse<GstSummaryReport>>('/reports/gst', {
      params: { startDate, endDate },
    });
    return res.data;
  },
};

// =================== NOTIFICATION SERVICE ===================
export const notificationService = {
  getAll: async (params?: { page?: number; size?: number }) => {
    const res = await api.get<ApiResponse<PagedResponse<Notification>>>('/notifications', { params });
    return res.data;
  },
  getUnread: async () => {
    const res = await api.get<ApiResponse<Notification[]>>('/notifications/unread');
    return res.data;
  },
  markAsRead: async (id: number) => {
    const res = await api.post<ApiResponse<void>>(`/notifications/${id}/read`);
    return res.data;
  },
  markAllAsRead: async () => {
    const res = await api.post<ApiResponse<void>>('/notifications/read-all');
    return res.data;
  },
};

// =================== SETTINGS SERVICE ===================
export const settingsService = {
  get: async () => {
    const res = await api.get<ApiResponse<StoreSettings>>('/settings');
    return res.data;
  },
  update: async (data: StoreSettings) => {
    const res = await api.put<ApiResponse<StoreSettings>>('/settings', data);
    return res.data;
  },
};

// =================== AUDIT SERVICE ===================
export const auditService = {
  getAll: async (params?: { page?: number; size?: number; entityName?: string }) => {
    const res = await api.get<ApiResponse<PagedResponse<AuditLog>>>('/audit-logs', { params });
    return res.data;
  },
};

export default api;
