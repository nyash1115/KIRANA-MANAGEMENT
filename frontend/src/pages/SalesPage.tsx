import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Stack,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  InputAdornment,
  CircularProgress,
  TablePagination,
  IconButton,
  Grid,
  MenuItem,
  Alert,
  Tooltip,
} from '@mui/material';
import { Search, Printer, RotateCcw, Ban, FileText } from 'lucide-react';
import { saleService } from '../services/api';
import { Sale, SaleReturnRequest, SaleReturnItemRequest } from '../types';
import { PrintInvoiceModal } from '../components/common/PrintInvoiceModal';

export const SalesPage: React.FC = () => {
  const [sales, setSales] = useState<Sale[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  // Search & Filter
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Modals
  const [selectedSale, setSelectedSale] = useState<Sale | null>(null);
  const [printOpen, setPrintOpen] = useState(false);

  // Return Modal
  const [returnOpen, setReturnOpen] = useState(false);
  const [returnItems, setReturnItems] = useState<{ saleItemId: number; maxQty: number; quantity: number; reason: string }[]>([]);
  const [refundMethod, setRefundMethod] = useState<'CASH' | 'UPI' | 'KHATA_CREDIT'>('CASH');
  const [submittingReturn, setSubmittingReturn] = useState(false);
  const [returnError, setReturnError] = useState<string | null>(null);

  // Cancel Modal
  const [cancelOpen, setCancelOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [submittingCancel, setSubmittingCancel] = useState(false);
  const [cancelError, setCancelError] = useState<string | null>(null);

  useEffect(() => {
    loadSales();
  }, [page, rowsPerPage, search, statusFilter]);

  const loadSales = async () => {
    try {
      setLoading(true);
      const res = await saleService.getAll({
        page,
        size: rowsPerPage,
        invoiceNumber: search.trim() || undefined,
        status: statusFilter || undefined,
      });
      setSales(res.data.content);
      setTotalElements(res.data.totalElements);
    } catch (e) {
      console.error('Failed to load sales', e);
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = (sale: Sale) => {
    setSelectedSale(sale);
    setPrintOpen(true);
  };

  const handleOpenReturn = (sale: Sale) => {
    setSelectedSale(sale);
    setReturnItems(
      sale.items.map((i) => ({
        saleItemId: i.id,
        maxQty: i.quantity,
        quantity: 0,
        reason: 'CUSTOMER_CHANGE_OF_MIND',
      }))
    );
    setRefundMethod('CASH');
    setReturnError(null);
    setReturnOpen(true);
  };

  const handleProcessReturn = async () => {
    if (!selectedSale) return;
    const activeReturns = returnItems.filter((i) => i.quantity > 0);
    if (activeReturns.length === 0) {
      setReturnError('Please enter return quantity > 0 for at least one item.');
      return;
    }

    try {
      setSubmittingReturn(true);
      setReturnError(null);
      const payload: SaleReturnRequest = {
        storeId: 1,
        items: activeReturns.map((i) => ({
          saleItemId: i.saleItemId,
          quantity: i.quantity,
          reason: i.reason,
          restockInventory: true,
        })),
        refundMethod,
        notes: 'POS customer sales return',
      };

      await saleService.processReturn(selectedSale.id, payload);
      setReturnOpen(false);
      loadSales();
    } catch (err: any) {
      setReturnError(err?.response?.data?.message || 'Failed to process return.');
    } finally {
      setSubmittingReturn(false);
    }
  };

  const handleOpenCancel = (sale: Sale) => {
    setSelectedSale(sale);
    setCancelReason('');
    setCancelError(null);
    setCancelOpen(true);
  };

  const handleProcessCancel = async () => {
    if (!selectedSale) return;
    if (!cancelReason.trim()) {
      setCancelError('Please specify reason for cancelling the invoice.');
      return;
    }

    try {
      setSubmittingCancel(true);
      setCancelError(null);
      await saleService.cancelSale(selectedSale.id, cancelReason);
      setCancelOpen(false);
      loadSales();
    } catch (err: any) {
      setCancelError(err?.response?.data?.message || 'Failed to cancel invoice.');
    } finally {
      setSubmittingCancel(false);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            Sales Invoices & Customer Returns
          </Typography>
          <Typography variant="body2" color="text.secondary">
            View completed transactions, reprint thermal receipts, process returns, or void invoices
          </Typography>
        </Box>
      </Stack>

      {/* Filter Bar */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={5}>
            <TextField
              size="small"
              fullWidth
              placeholder="Search by Invoice Number..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search size={18} color="#64748b" />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              select
              size="small"
              fullWidth
              label="Invoice Status Filter"
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
            >
              <MenuItem value="">All Statuses</MenuItem>
              <MenuItem value="COMPLETED">Completed</MenuItem>
              <MenuItem value="PARTIALLY_RETURNED">Partially Returned</MenuItem>
              <MenuItem value="RETURNED">Fully Returned</MenuItem>
              <MenuItem value="CANCELLED">Cancelled / Voided</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} md={3} sx={{ textAlign: { md: 'right' } }}>
            <Typography variant="body2" color="text.secondary">
              Total Invoices: <strong>{totalElements}</strong>
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* Sales Table */}
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Invoice #</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Date & Time</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Customer</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Cashier</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Grand Total</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Payment Method</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Status</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={32} />
                  </TableCell>
                </TableRow>
              ) : sales.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No sales invoices found.
                  </TableCell>
                </TableRow>
              ) : (
                sales.map((s) => {
                  const paymentChips = s.payments.map((p) => p.paymentMethod).join(', ');
                  return (
                    <TableRow key={s.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight={700}>
                          {s.invoiceNumber}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {new Date(s.createdAt).toLocaleDateString()}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(s.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight={600}>
                          {s.customerName || 'Walk-in Customer'}
                        </Typography>
                        {s.customerPhone && (
                          <Typography variant="caption" color="text.secondary">
                            {s.customerPhone}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>{s.cashierName}</TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" fontWeight={800} color="primary.main">
                          ₹{s.grandTotal}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip size="small" label={paymentChips} sx={{ fontWeight: 600 }} />
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          size="small"
                          label={s.status}
                          color={
                            s.status === 'COMPLETED'
                              ? 'success'
                              : s.status === 'CANCELLED'
                              ? 'error'
                              : 'warning'
                          }
                          sx={{ fontWeight: 700 }}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Stack direction="row" spacing={0.5} justifyContent="center">
                          <Tooltip title="Print / View Invoice">
                            <IconButton size="small" color="primary" onClick={() => handlePrint(s)}>
                              <Printer size={16} />
                            </IconButton>
                          </Tooltip>
                          {s.status === 'COMPLETED' && (
                            <>
                              <Tooltip title="Process Return">
                                <IconButton size="small" color="warning" onClick={() => handleOpenReturn(s)}>
                                  <RotateCcw size={16} />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Cancel / Void Invoice">
                                <IconButton size="small" color="error" onClick={() => handleOpenCancel(s)}>
                                  <Ban size={16} />
                                </IconButton>
                              </Tooltip>
                            </>
                          )}
                        </Stack>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[10, 25, 50]}
          component="div"
          count={totalElements}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
        />
      </Paper>

      {/* Return Dialog */}
      <Dialog
        open={returnOpen}
        onClose={() => setReturnOpen(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          Process Sales Return • Invoice {selectedSale?.invoiceNumber}
        </DialogTitle>
        <DialogContent dividers>
          {returnError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {returnError}
            </Alert>
          )}

          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Specify the return quantity for each item. Restocked items will immediately increment store inventory.
          </Typography>

          <TableContainer component={Paper} variant="outlined" sx={{ mb: 3 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 700 }}>Product</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 700 }}>Sold Qty</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 700 }}>Return Qty</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Reason</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {selectedSale?.items.map((item, idx) => {
                  const stateItem = returnItems[idx];
                  return (
                    <TableRow key={item.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight={600}>
                          {item.productName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Unit Price: ₹{item.unitPrice}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">{item.quantity}</TableCell>
                      <TableCell align="center">
                        <TextField
                          type="number"
                          size="small"
                          sx={{ width: 80 }}
                          value={stateItem?.quantity || 0}
                          onChange={(e) => {
                            const val = Math.min(item.quantity, Math.max(0, Number(e.target.value)));
                            setReturnItems((prev) => {
                              const copy = [...prev];
                              copy[idx] = { ...copy[idx], quantity: val };
                              return copy;
                            });
                          }}
                        />
                      </TableCell>
                      <TableCell>
                        <TextField
                          select
                          size="small"
                          fullWidth
                          value={stateItem?.reason || 'CUSTOMER_CHANGE_OF_MIND'}
                          onChange={(e) => {
                            setReturnItems((prev) => {
                              const copy = [...prev];
                              copy[idx] = { ...copy[idx], reason: e.target.value };
                              return copy;
                            });
                          }}
                        >
                          <MenuItem value="CUSTOMER_CHANGE_OF_MIND">Customer Changed Mind</MenuItem>
                          <MenuItem value="DAMAGED">Damaged / Leaking</MenuItem>
                          <MenuItem value="EXPIRED">Quality / Expired</MenuItem>
                          <MenuItem value="WRONG_ITEM">Wrong Item Billed</MenuItem>
                        </TextField>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>

          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={6}>
              <TextField
                select
                fullWidth
                label="Refund Method"
                value={refundMethod}
                onChange={(e) => setRefundMethod(e.target.value as any)}
              >
                <MenuItem value="CASH">Cash Refund</MenuItem>
                <MenuItem value="UPI">UPI Refund</MenuItem>
                <MenuItem value="KHATA_CREDIT">Credit to Customer Khata</MenuItem>
              </TextField>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setReturnOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            color="warning"
            onClick={handleProcessReturn}
            disabled={submittingReturn}
            sx={{ fontWeight: 700 }}
          >
            {submittingReturn ? <CircularProgress size={20} /> : 'Confirm Sales Return'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Cancel Dialog */}
      <Dialog
        open={cancelOpen}
        onClose={() => setCancelOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          Cancel Bill • {selectedSale?.invoiceNumber}
        </DialogTitle>
        <DialogContent dividers>
          {cancelError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {cancelError}
            </Alert>
          )}

          <Alert severity="warning" sx={{ mb: 2 }}>
            Cancelling this invoice will reverse all inventory deductions and Khata transactions. This action is permanently audited.
          </Alert>

          <TextField
            autoFocus
            required
            fullWidth
            multiline
            rows={3}
            label="Reason for Cancellation"
            value={cancelReason}
            onChange={(e) => setCancelReason(e.target.value)}
            placeholder="e.g. Customer card declined after checkout / Entered wrong items"
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setCancelOpen(false)}>Back</Button>
          <Button
            variant="contained"
            color="error"
            onClick={handleProcessCancel}
            disabled={submittingCancel}
            sx={{ fontWeight: 700 }}
          >
            {submittingCancel ? <CircularProgress size={20} /> : 'Confirm Cancellation'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Print Modal */}
      {selectedSale && (
        <PrintInvoiceModal
          open={printOpen}
          onClose={() => setPrintOpen(false)}
          sale={selectedSale}
        />
      )}
    </Box>
  );
};
