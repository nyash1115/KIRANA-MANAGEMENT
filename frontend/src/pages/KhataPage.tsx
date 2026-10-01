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
  Card,
  CardContent,
  Alert,
  Tooltip,
  IconButton,
} from '@mui/material';
import { BookOpen, DollarSign, History, Plus, Phone, Search } from 'lucide-react';
import { customerService } from '../services/api';
import { Customer, KhataTransaction, CustomerPaymentRequest } from '../types';

export const KhataPage: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');

  // Payment Recording Dialog
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [paymentAmount, setPaymentAmount] = useState<number>(0);
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'UPI' | 'BANK_TRANSFER'>('CASH');
  const [paymentNotes, setPaymentNotes] = useState('');
  const [submittingPayment, setSubmittingPayment] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  // Statement History Dialog
  const [statementOpen, setStatementOpen] = useState(false);
  const [transactions, setTransactions] = useState<KhataTransaction[]>([]);
  const [loadingStatement, setLoadingStatement] = useState(false);

  useEffect(() => {
    loadCustomers();
  }, [page, rowsPerPage, search]);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const res = await customerService.getAll({
        page,
        size: rowsPerPage,
        search: search.trim() || undefined,
      });
      setCustomers(res.data.content);
      setTotalElements(res.data.totalElements);
    } catch (e) {
      console.error('Failed to load customers', e);
    } finally {
      setLoading(false);
    }
  };

  const totalOutstanding = customers.reduce((acc, c) => acc + (c.outstandingBalance || 0), 0);

  const handleOpenPayment = (customer: Customer) => {
    setSelectedCustomer(customer);
    setPaymentAmount(customer.outstandingBalance);
    setPaymentMethod('CASH');
    setPaymentNotes('');
    setPaymentError(null);
    setPaymentOpen(true);
  };

  const handleSavePayment = async () => {
    if (!selectedCustomer) return;
    if (paymentAmount <= 0) {
      setPaymentError('Payment amount must be greater than 0.');
      return;
    }

    try {
      setSubmittingPayment(true);
      setPaymentError(null);
      const payload: CustomerPaymentRequest = {
        storeId: 1,
        amount: paymentAmount,
        paymentMethod,
        notes: paymentNotes || 'Udhaar Khata Repayment',
      };
      await customerService.recordPayment(selectedCustomer.id, payload);
      setPaymentOpen(false);
      loadCustomers();
    } catch (err: any) {
      setPaymentError(err?.response?.data?.message || 'Failed to record payment.');
    } finally {
      setSubmittingPayment(false);
    }
  };

  const handleOpenStatement = async (customer: Customer) => {
    setSelectedCustomer(customer);
    setStatementOpen(true);
    try {
      setLoadingStatement(true);
      const res = await customerService.getKhata(customer.id);
      setTransactions(res.data);
    } catch (e) {
      console.error('Failed to load statement', e);
    } finally {
      setLoadingStatement(false);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            Customer Khata & Udhaar Ledger
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage customer credit accounts, track debit/credit ledger, and record partial/full repayments
          </Typography>
        </Box>
      </Stack>

      {/* KPI Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={4}>
          <Card elevation={2} sx={{ borderRadius: 3, borderLeft: '5px solid #f59e0b' }}>
            <CardContent sx={{ p: 2 }}>
              <Typography variant="caption" color="text.secondary" fontWeight={600}>
                TOTAL KHATA RECEIVABLES
              </Typography>
              <Typography variant="h4" fontWeight={800} color="#f59e0b" sx={{ mt: 0.5 }}>
                ₹{totalOutstanding.toLocaleString('en-IN')}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Active customer credit balance
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filter / Search Bar */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6}>
            <TextField
              size="small"
              fullWidth
              placeholder="Search customer by name or phone..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              InputProps={{
                startAdornment: <Search size={18} color="#64748b" style={{ marginRight: 8 }} />,
              }}
            />
          </Grid>
          <Grid item xs={12} sm={6} sx={{ textAlign: { sm: 'right' } }}>
            <Typography variant="body2" color="text.secondary">
              Total Khata Customers: <strong>{totalElements}</strong>
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* Customers Table */}
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Customer Name</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Phone</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Credit Limit</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Outstanding Due (₹)</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Credit Status</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={32} />
                  </TableCell>
                </TableRow>
              ) : customers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No customers found.
                  </TableCell>
                </TableRow>
              ) : (
                customers.map((c) => {
                  const hasDue = c.outstandingBalance > 0;
                  return (
                    <TableRow key={c.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight={700}>
                          {c.name}
                        </Typography>
                        {c.address && (
                          <Typography variant="caption" color="text.secondary">
                            {c.address}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <Stack direction="row" spacing={0.5} alignItems="center">
                          <Phone size={14} color="#64748b" />
                          <Typography variant="body2">{c.phone}</Typography>
                        </Stack>
                      </TableCell>
                      <TableCell align="right">₹{c.creditLimit}</TableCell>
                      <TableCell align="right">
                        <Typography
                          variant="body1"
                          fontWeight={800}
                          color={hasDue ? 'warning.dark' : 'success.main'}
                        >
                          ₹{c.outstandingBalance}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          size="small"
                          label={hasDue ? 'PAYMENT DUE' : 'CLEAR'}
                          color={hasDue ? 'warning' : 'success'}
                          sx={{ fontWeight: 700 }}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Stack direction="row" spacing={1} justifyContent="center">
                          <Button
                            size="small"
                            variant="contained"
                            color="primary"
                            disabled={!hasDue}
                            onClick={() => handleOpenPayment(c)}
                            sx={{ textTransform: 'none', fontWeight: 600 }}
                          >
                            Receive Payment
                          </Button>
                          <Button
                            size="small"
                            variant="outlined"
                            startIcon={<History size={14} />}
                            onClick={() => handleOpenStatement(c)}
                            sx={{ textTransform: 'none', fontWeight: 600 }}
                          >
                            Ledger
                          </Button>
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

      {/* Record Repayment Dialog */}
      <Dialog
        open={paymentOpen}
        onClose={() => setPaymentOpen(false)}
        maxWidth="xs"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>Record Khata Repayment</DialogTitle>
        <DialogContent dividers>
          {paymentError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {paymentError}
            </Alert>
          )}

          {selectedCustomer && (
            <Box sx={{ mb: 2, p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
              <Typography variant="body2">
                Customer: <strong>{selectedCustomer.name}</strong>
              </Typography>
              <Typography variant="body2" color="warning.dark" fontWeight={700}>
                Current Balance Due: ₹{selectedCustomer.outstandingBalance}
              </Typography>
            </Box>
          )}

          <TextField
            autoFocus
            required
            fullWidth
            type="number"
            label="Amount Paid (₹)"
            value={paymentAmount}
            onChange={(e) => setPaymentAmount(Number(e.target.value))}
            sx={{ mb: 2 }}
          />

          <TextField
            select
            fullWidth
            label="Payment Mode"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value as any)}
            sx={{ mb: 2 }}
          >
            <MenuItem value="CASH">Cash</MenuItem>
            <MenuItem value="UPI">UPI (GPay / PhonePe / Paytm)</MenuItem>
            <MenuItem value="BANK_TRANSFER">Direct Bank Transfer</MenuItem>
          </TextField>

          <TextField
            fullWidth
            multiline
            rows={2}
            label="Notes / Reference"
            value={paymentNotes}
            onChange={(e) => setPaymentNotes(e.target.value)}
            placeholder="e.g. Paid in cash at counter"
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setPaymentOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSavePayment}
            disabled={submittingPayment}
            sx={{ fontWeight: 700 }}
          >
            {submittingPayment ? <CircularProgress size={20} /> : 'Confirm & Update Ledger'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Ledger Statement Modal */}
      <Dialog
        open={statementOpen}
        onClose={() => setStatementOpen(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          Account Ledger Statement • {selectedCustomer?.name}
        </DialogTitle>
        <DialogContent dividers>
          {loadingStatement ? (
            <Box sx={{ py: 6, textAlign: 'center' }}>
              <CircularProgress size={32} />
            </Box>
          ) : transactions.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
              No transactions recorded for this customer yet.
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>Date</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Type</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Reference</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Debit (+)</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Credit (-)</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Running Balance</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transactions.map((tx) => (
                    <TableRow key={tx.id}>
                      <TableCell>{new Date(tx.createdAt).toLocaleDateString()}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={tx.transactionType}
                          color={tx.transactionType === 'DEBIT' ? 'warning' : 'success'}
                          sx={{ fontWeight: 700 }}
                        />
                      </TableCell>
                      <TableCell>{tx.referenceNumber || 'N/A'}</TableCell>
                      <TableCell align="right" sx={{ color: tx.transactionType === 'DEBIT' ? 'warning.dark' : 'inherit', fontWeight: 600 }}>
                        {tx.transactionType === 'DEBIT' ? `₹${tx.amount}` : '-'}
                      </TableCell>
                      <TableCell align="right" sx={{ color: tx.transactionType === 'CREDIT' ? 'success.main' : 'inherit', fontWeight: 600 }}>
                        {tx.transactionType === 'CREDIT' ? `₹${tx.amount}` : '-'}
                      </TableCell>
                      <TableCell align="right" sx={{ fontWeight: 800 }}>
                        ₹{tx.runningBalance}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setStatementOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
