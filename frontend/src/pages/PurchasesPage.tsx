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
  MenuItem,
  CircularProgress,
  TablePagination,
  Grid,
  IconButton,
  Divider,
  Alert,
} from '@mui/material';
import { Truck, Plus, Trash2, Calendar, FileText } from 'lucide-react';
import { purchaseService, supplierService, productService } from '../services/api';
import { Purchase, Supplier, Product, PurchaseRequest, PurchaseItemRequest } from '../types';

export const PurchasesPage: React.FC = () => {
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  // New Purchase Dialog State
  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const [supplierId, setSupplierId] = useState<number | ''>('');
  const [supplierInvoiceNo, setSupplierInvoiceNo] = useState('');
  const [purchaseDate, setPurchaseDate] = useState(new Date().toISOString().split('T')[0]);
  const [items, setItems] = useState<PurchaseItemRequest[]>([]);
  const [paymentStatus, setPaymentStatus] = useState<'PAID' | 'PARTIAL' | 'UNPAID'>('PAID');
  const [amountPaid, setAmountPaid] = useState<number>(0);

  useEffect(() => {
    loadLookups();
  }, []);

  useEffect(() => {
    loadPurchases();
  }, [page, rowsPerPage]);

  const loadLookups = async () => {
    try {
      const [supRes, prodRes] = await Promise.all([
        supplierService.getAll({ size: 100 }),
        productService.getAll({ size: 200 }),
      ]);
      setSuppliers(supRes.data.content);
      setProducts(prodRes.data.content);
    } catch (e) {
      console.error('Failed to load suppliers/products', e);
    }
  };

  const loadPurchases = async () => {
    try {
      setLoading(true);
      const res = await purchaseService.getAll({
        page,
        size: rowsPerPage,
      });
      setPurchases(res.data.content);
      setTotalElements(res.data.totalElements);
    } catch (e) {
      console.error('Failed to load purchases', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenNewPurchase = () => {
    setSupplierId(suppliers[0]?.id || '');
    setSupplierInvoiceNo('INV-' + Math.floor(10000 + Math.random() * 90000));
    setPurchaseDate(new Date().toISOString().split('T')[0]);
    // default 1 item
    if (products.length > 0) {
      const p = products[0];
      setItems([
        {
          productId: p.id,
          quantity: 20,
          costPrice: p.defaultCostPrice,
          sellingPrice: p.defaultSellingPrice,
          mrp: p.defaultMrp,
          batchNumber: 'LOT-' + new Date().toISOString().slice(2, 7).replace('-', '') + '-01',
          expiryDate: new Date(Date.now() + 180 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          gstRate: p.gstRate,
        },
      ]);
    }
    setPaymentStatus('PAID');
    setFormError(null);
    setModalOpen(true);
  };

  const handleAddItem = () => {
    if (products.length === 0) return;
    const p = products[0];
    setItems((prev) => [
      ...prev,
      {
        productId: p.id,
        quantity: 10,
        costPrice: p.defaultCostPrice,
        sellingPrice: p.defaultSellingPrice,
        mrp: p.defaultMrp,
        batchNumber: 'LOT-' + Math.floor(100 + Math.random() * 900),
        expiryDate: new Date(Date.now() + 180 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        gstRate: p.gstRate,
      },
    ]);
  };

  const handleUpdateItem = (index: number, field: keyof PurchaseItemRequest, val: any) => {
    setItems((prev) => {
      const copy = [...prev];
      const current = { ...copy[index], [field]: val };
      // If product changed, update default prices
      if (field === 'productId') {
        const prod = products.find((p) => p.id === val);
        if (prod) {
          current.costPrice = prod.defaultCostPrice;
          current.sellingPrice = prod.defaultSellingPrice;
          current.mrp = prod.defaultMrp;
          current.gstRate = prod.gstRate;
        }
      }
      copy[index] = current;
      return copy;
    });
  };

  const handleRemoveItem = (index: number) => {
    setItems((prev) => prev.filter((_, i) => i !== index));
  };

  // Calculations
  const calculateTotal = () => {
    let subtotal = 0;
    let taxTotal = 0;
    items.forEach((item) => {
      const lineSub = item.quantity * item.costPrice;
      const lineTax = (lineSub * item.gstRate) / 100;
      subtotal += lineSub;
      taxTotal += lineTax;
    });
    const grandTotal = Math.round(subtotal + taxTotal);
    return { subtotal, taxTotal, grandTotal };
  };

  const totals = calculateTotal();

  const handleSavePurchase = async () => {
    if (!supplierId) {
      setFormError('Please select a supplier.');
      return;
    }
    if (items.length === 0) {
      setFormError('Please add at least one product.');
      return;
    }

    try {
      setSubmitting(true);
      setFormError(null);

      let actualPaid = amountPaid;
      if (paymentStatus === 'PAID') {
        actualPaid = totals.grandTotal;
      } else if (paymentStatus === 'UNPAID') {
        actualPaid = 0;
      }

      const payload: PurchaseRequest = {
        storeId: 1,
        supplierId: Number(supplierId),
        supplierInvoiceNumber: supplierInvoiceNo,
        purchaseDate,
        items,
        paymentStatus,
        amountPaid: actualPaid,
        notes: 'Inward purchase receipt',
      };

      await purchaseService.create(payload);
      setModalOpen(false);
      loadPurchases();
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to record purchase.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            Inward Stock Purchases & Supplier Invoices
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Receive stock from distributors, auto-generate batch inventory, and record supplier payables
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Plus size={18} />}
          onClick={handleOpenNewPurchase}
          sx={{ fontWeight: 700, px: 2.5 }}
        >
          Inward Stock (New Purchase)
        </Button>
      </Stack>

      {/* Purchases Table */}
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Purchase #</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Supplier</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Supplier Invoice #</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Date</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Total Amount</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Paid Amount</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Payment Status</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={32} />
                  </TableCell>
                </TableRow>
              ) : purchases.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No purchase orders recorded yet.
                  </TableCell>
                </TableRow>
              ) : (
                purchases.map((pur) => (
                  <TableRow key={pur.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={700}>
                        {pur.purchaseNumber}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600}>
                        {pur.supplierName}
                      </Typography>
                    </TableCell>
                    <TableCell>{pur.supplierInvoiceNumber || 'N/A'}</TableCell>
                    <TableCell>{pur.purchaseDate}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>
                      ₹{pur.totalAmount}
                    </TableCell>
                    <TableCell align="right">₹{pur.amountPaid}</TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={pur.paymentStatus}
                        color={
                          pur.paymentStatus === 'PAID'
                            ? 'success'
                            : pur.paymentStatus === 'PARTIAL'
                            ? 'warning'
                            : 'error'
                        }
                        sx={{ fontWeight: 600 }}
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Chip size="small" label={pur.status} variant="outlined" />
                    </TableCell>
                  </TableRow>
                ))
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

      {/* New Purchase Dialog */}
      <Dialog
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        maxWidth="lg"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>Inward Stock Purchase Order (GRN)</DialogTitle>
        <DialogContent dividers>
          {formError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {formError}
            </Alert>
          )}

          {/* Supplier Header */}
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid item xs={12} sm={4}>
              <TextField
                select
                required
                fullWidth
                label="Supplier / Distributor"
                value={supplierId}
                onChange={(e) => setSupplierId(Number(e.target.value))}
              >
                {suppliers.map((s) => (
                  <MenuItem key={s.id} value={s.id}>
                    {s.name} (Due: ₹{s.outstandingBalance})
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Supplier Bill / Invoice Number"
                value={supplierInvoiceNo}
                onChange={(e) => setSupplierInvoiceNo(e.target.value)}
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                type="date"
                fullWidth
                label="Purchase Date"
                value={purchaseDate}
                onChange={(e) => setPurchaseDate(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
          </Grid>

          <Divider sx={{ mb: 2 }} />

          {/* Line items table */}
          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
            <Typography variant="subtitle2" fontWeight={700}>
              Product Line Items
            </Typography>
            <Button size="small" startIcon={<Plus size={16} />} onClick={handleAddItem}>
              Add Line Item
            </Button>
          </Stack>

          <TableContainer component={Paper} variant="outlined" sx={{ mb: 3 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 700, width: '25%' }}>Product</TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '12%' }}>Batch #</TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '14%' }}>Expiry Date</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 700, width: '10%' }}>Qty</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 700, width: '12%' }}>Cost (₹)</TableCell>
                  <TableCell align="right" sx={{ fontWeight: 700, width: '12%' }}>Selling (₹)</TableCell>
                  <TableCell align="center" sx={{ fontWeight: 700, width: '8%' }}>GST %</TableCell>
                  <TableCell align="center" sx={{ width: '7%' }}></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {items.map((item, idx) => (
                  <TableRow key={idx}>
                    <TableCell>
                      <TextField
                        select
                        size="small"
                        fullWidth
                        value={item.productId}
                        onChange={(e) => handleUpdateItem(idx, 'productId', Number(e.target.value))}
                      >
                        {products.map((p) => (
                          <MenuItem key={p.id} value={p.id}>
                            {p.name}
                          </MenuItem>
                        ))}
                      </TextField>
                    </TableCell>
                    <TableCell>
                      <TextField
                        size="small"
                        fullWidth
                        value={item.batchNumber}
                        onChange={(e) => handleUpdateItem(idx, 'batchNumber', e.target.value)}
                      />
                    </TableCell>
                    <TableCell>
                      <TextField
                        type="date"
                        size="small"
                        fullWidth
                        value={item.expiryDate || ''}
                        onChange={(e) => handleUpdateItem(idx, 'expiryDate', e.target.value)}
                      />
                    </TableCell>
                    <TableCell align="center">
                      <TextField
                        type="number"
                        size="small"
                        value={item.quantity}
                        onChange={(e) => handleUpdateItem(idx, 'quantity', Number(e.target.value))}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <TextField
                        type="number"
                        size="small"
                        value={item.costPrice}
                        onChange={(e) => handleUpdateItem(idx, 'costPrice', Number(e.target.value))}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <TextField
                        type="number"
                        size="small"
                        value={item.sellingPrice}
                        onChange={(e) => handleUpdateItem(idx, 'sellingPrice', Number(e.target.value))}
                      />
                    </TableCell>
                    <TableCell align="center">
                      <TextField
                        type="number"
                        size="small"
                        value={item.gstRate}
                        onChange={(e) => handleUpdateItem(idx, 'gstRate', Number(e.target.value))}
                      />
                    </TableCell>
                    <TableCell align="center">
                      <IconButton size="small" color="error" onClick={() => handleRemoveItem(idx)}>
                        <Trash2 size={16} />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          {/* Payment & Totals */}
          <Grid container spacing={3} alignItems="center">
            <Grid item xs={12} sm={4}>
              <TextField
                select
                fullWidth
                label="Payment Status"
                value={paymentStatus}
                onChange={(e) => setPaymentStatus(e.target.value as any)}
              >
                <MenuItem value="PAID">Paid Immediately in Full</MenuItem>
                <MenuItem value="PARTIAL">Partially Paid</MenuItem>
                <MenuItem value="UNPAID">Credit / Unpaid (Add to Supplier Balance)</MenuItem>
              </TextField>
            </Grid>
            {paymentStatus === 'PARTIAL' && (
              <Grid item xs={12} sm={4}>
                <TextField
                  type="number"
                  fullWidth
                  label="Amount Paid Now (₹)"
                  value={amountPaid}
                  onChange={(e) => setAmountPaid(Number(e.target.value))}
                />
              </Grid>
            )}
            <Grid item xs={12} sm={paymentStatus === 'PARTIAL' ? 4 : 8} sx={{ textAlign: 'right' }}>
              <Typography variant="body2" color="text.secondary">
                Subtotal: ₹{totals.subtotal.toFixed(2)} | GST: ₹{totals.taxTotal.toFixed(2)}
              </Typography>
              <Typography variant="h5" fontWeight={800} color="primary.main">
                Grand Total: ₹{totals.grandTotal}
              </Typography>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setModalOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSavePurchase}
            disabled={submitting}
            sx={{ px: 3, fontWeight: 700 }}
          >
            {submitting ? <CircularProgress size={20} /> : 'Save Purchase & Receive Stock'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
