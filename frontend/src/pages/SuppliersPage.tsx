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
  CircularProgress,
  TablePagination,
  Grid,
  Alert,
} from '@mui/material';
import { Plus, Phone, Mail, Building2, Search } from 'lucide-react';
import { supplierService } from '../services/api';
import { Supplier, SupplierRequest } from '../types';

export const SuppliersPage: React.FC = () => {
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');

  // Dialog State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<Supplier | null>(null);
  const [formData, setFormData] = useState<SupplierRequest>({
    name: '',
    contactPerson: '',
    phone: '',
    email: '',
    address: '',
    gstin: '',
    notes: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    loadSuppliers();
  }, [page, rowsPerPage, search]);

  const loadSuppliers = async () => {
    try {
      setLoading(true);
      const res = await supplierService.getAll({
        page,
        size: rowsPerPage,
        search: search.trim() || undefined,
      });
      setSuppliers(res.data.content);
      setTotalElements(res.data.totalElements);
    } catch (e) {
      console.error('Failed to load suppliers', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingSupplier(null);
    setFormData({
      name: '',
      contactPerson: '',
      phone: '',
      email: '',
      address: '',
      gstin: '',
      notes: '',
    });
    setFormError(null);
    setModalOpen(true);
  };

  const handleSaveSupplier = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim() || !formData.phone.trim()) {
      setFormError('Supplier Name and Phone are required.');
      return;
    }

    try {
      setSubmitting(true);
      setFormError(null);
      if (editingSupplier) {
        await supplierService.update(editingSupplier.id, formData);
      } else {
        await supplierService.create(formData);
      }
      setModalOpen(false);
      loadSuppliers();
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to save supplier.');
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
            Distributors & FMCG Suppliers
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage wholesale vendor contacts, GSTIN numbers, and pending payable balances
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Plus size={18} />}
          onClick={handleOpenAdd}
          sx={{ fontWeight: 700, px: 2.5 }}
        >
          Add Supplier
        </Button>
      </Stack>

      {/* Filter / Search Bar */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6}>
            <TextField
              size="small"
              fullWidth
              placeholder="Search supplier by agency name or phone..."
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
              Total Suppliers: <strong>{totalElements}</strong>
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* Suppliers Table */}
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Agency / Supplier Name</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Contact Person</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Phone / Email</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>GSTIN</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Pending Payables (₹)</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={32} />
                  </TableCell>
                </TableRow>
              ) : suppliers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No suppliers found.
                  </TableCell>
                </TableRow>
              ) : (
                suppliers.map((s) => (
                  <TableRow key={s.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={700}>
                        {s.name}
                      </Typography>
                      {s.address && (
                        <Typography variant="caption" color="text.secondary">
                          {s.address}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>{s.contactPerson || 'N/A'}</TableCell>
                    <TableCell>
                      <Stack direction="row" spacing={0.5} alignItems="center">
                        <Phone size={14} color="#64748b" />
                        <Typography variant="body2">{s.phone}</Typography>
                      </Stack>
                      {s.email && (
                        <Stack direction="row" spacing={0.5} alignItems="center" mt={0.5}>
                          <Mail size={14} color="#64748b" />
                          <Typography variant="caption" color="text.secondary">
                            {s.email}
                          </Typography>
                        </Stack>
                      )}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                        {s.gstin || 'Unregistered'}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography
                        variant="body1"
                        fontWeight={800}
                        color={s.outstandingBalance > 0 ? 'warning.dark' : 'text.primary'}
                      >
                        ₹{s.outstandingBalance}
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={s.outstandingBalance > 0 ? 'PAYABLE DUE' : 'CLEAR'}
                        color={s.outstandingBalance > 0 ? 'warning' : 'success'}
                        sx={{ fontWeight: 700 }}
                      />
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

      {/* Add / Edit Supplier Dialog */}
      <Dialog
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          {editingSupplier ? `Edit Supplier: ${editingSupplier.name}` : 'Add New Supplier Agency'}
        </DialogTitle>
        <DialogContent dividers>
          {formError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {formError}
            </Alert>
          )}

          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                required
                fullWidth
                label="Agency / Supplier Name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g. Pune FMCG Wholesalers Ltd"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Contact Person"
                value={formData.contactPerson}
                onChange={(e) => setFormData({ ...formData, contactPerson: e.target.value })}
                placeholder="e.g. Rakesh Kumar"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                required
                fullWidth
                label="Phone Number"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                placeholder="e.g. 9876543210"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="GSTIN"
                value={formData.gstin}
                onChange={(e) => setFormData({ ...formData, gstin: e.target.value })}
                placeholder="e.g. 27AAAAA0000A1Z5"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Address / Godown Location"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setModalOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSaveSupplier}
            disabled={submitting}
            sx={{ px: 3, fontWeight: 700 }}
          >
            {submitting ? <CircularProgress size={20} /> : 'Save Supplier'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
