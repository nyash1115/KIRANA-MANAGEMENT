import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import { AppLayout } from './components/layout/AppLayout';

// Pages
import { LoginPage } from './pages/LoginPage';
import { PosPage } from './pages/PosPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProductsPage } from './pages/ProductsPage';
import { InventoryPage } from './pages/InventoryPage';
import { PurchasesPage } from './pages/PurchasesPage';
import { SalesPage } from './pages/SalesPage';
import { KhataPage } from './pages/KhataPage';
import { SuppliersPage } from './pages/SuppliersPage';
import { ReportsPage } from './pages/ReportsPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { SettingsPage } from './pages/SettingsPage';
import { AuditPage } from './pages/AuditPage';

const ProtectedRoute: React.FC<{ children: React.ReactNode; allowedRoles?: string[] }> = ({
  children,
  allowedRoles,
}) => {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/pos" replace />;
  }

  return <AppLayout>{children}</AppLayout>;
};

export const App: React.FC = () => {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Public route */}
            <Route path="/login" element={<LoginPage />} />

            {/* POS Billing - accessible to all authenticated roles (Admin, Manager, Cashier) */}
            <Route
              path="/pos"
              element={
                <ProtectedRoute>
                  <PosPage />
                </ProtectedRoute>
              }
            />

            {/* Dashboard - Admin and Manager */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />

            {/* Products Master */}
            <Route
              path="/products"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                  <ProductsPage />
                </ProtectedRoute>
              }
            />

            {/* Inventory Batches & Adjustments */}
            <Route
              path="/inventory"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                  <InventoryPage />
                </ProtectedRoute>
              }
            />

            {/* Inward Purchases */}
            <Route
              path="/purchases"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                  <PurchasesPage />
                </ProtectedRoute>
              }
            />

            {/* Sales Invoices, Returns & Void */}
            <Route
              path="/sales"
              element={
                <ProtectedRoute>
                  <SalesPage />
                </ProtectedRoute>
              }
            />

            {/* Customer Khata Ledger */}
            <Route
              path="/khata"
              element={
                <ProtectedRoute>
                  <KhataPage />
                </ProtectedRoute>
              }
            />

            {/* Suppliers */}
            <Route
              path="/suppliers"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                  <SuppliersPage />
                </ProtectedRoute>
              }
            />

            {/* Reports */}
            <Route
              path="/reports"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                  <ReportsPage />
                </ProtectedRoute>
              }
            />

            {/* Notifications */}
            <Route
              path="/notifications"
              element={
                <ProtectedRoute>
                  <NotificationsPage />
                </ProtectedRoute>
              }
            />

            {/* Settings - Admin only */}
            <Route
              path="/settings"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                  <SettingsPage />
                </ProtectedRoute>
              }
            />

            {/* Audit Logs - Admin only */}
            <Route
              path="/audit"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                  <AuditPage />
                </ProtectedRoute>
              }
            />

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/pos" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
};
