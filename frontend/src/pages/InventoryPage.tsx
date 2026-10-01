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
  Alert,
  Grid,
} from '@mui/material';
import { Layers, SlidersHorizontal, AlertTriangle, CheckCircle2, XCircle } from 'lucide-react';
import { inventoryService } from '../services/api';
import { InventoryBatch, StockAdjustmentRequest } from '../types';

export const InventoryPage: React.FC = () => {
  const [batches, setBatches] = useState<InventoryBatch[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string>('ACTIVE');

  // Adjustment Dialog
  const [adjustOpen, setAdjustOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState<InventoryBatch | null>(null);
  const [adjustQuantity, setAdjustQuantity] = useState<number>(0);
  const [adjustReason, setAdjustReason] = useState<StockAdjustmentRequest['reason']>('DAMAGED');
  const [adjustNotes, setAdjustNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [adjustError, setAdjustError] = useState<string | null>(null);

  useEffect(() => {
    loadBatches();
  }, [page, rowsPerPage, statusFilter]);

  const loadBatches = async () => {
    try {
      setLoading(true);
      const res = await inventoryService.getBatches({
        page,
        size: rowsPerPage,
        status: statusFilter || undefined,
      });
      setBatches(res.data.content);
      setTotalElements(res.data.totalElements);
    } catch (e) {
      console.error('Failed to load inventory batches', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdjust = (batch: InventoryBatch) => {
    setSelectedBatch(batch);
    setAdjustQuantity(0);
    setAdjustReason('DAMAGED');
    setAdjustNotes('');
    setAdjustError(null);
    setAdjustOpen(true);
  };

  const handleSaveAdjustment = async () => {
    if (!selectedBatch) return;
    if (adjustQuantity === 0) {
      setAdjustError('Adjustment quantity cannot be 0. Enter negative to reduce or positive to increase.');
      return;
    }
    if (selectedBatch.quantity + adjustQuantity < 0) {
      setAdjustError(`Cannot deduct more than available stock (${selectedBatch.quantity} ${selectedBatch.unitCode}).`);
      return;
    }

    try {
      setSubmitting(true);
      setAdjustError(null);
      await inventoryService.adjustStock({
        storeId: 1,
        productId: selectedBatch.productId,
        batchId: selectedBatch.id,
        quantityDelta: adjustQuantity,
        reason: adjustReason,
        notes: adjustNotes || 'Physical stock audit adjustment',
      });
      setAdjustOpen(false);
      loadBatches();
    } catch (err: any) {
      setAdjustError(err?.response?.data?.message || 'Failed to adjust stock.');
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
            Inventory & FEFO Batch Management
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Track multi-batch expiry dates, lot numbers, FIFO/FEFO dispatch order, and manual stock audits
          </Typography>
        </Box>
      </Stack>

      {/* Filter Bar */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            select
            size="small"
            label="Batch Status Filter"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            sx={{ minWidth: 200 }}
          >
            <MenuItem value="">All Batches</MenuItem>
            <MenuItem value="ACTIVE">Active Available Stock</MenuItem>
            <MenuItem value="DEPLETED">Depleted Batches (0 Qty)</MenuItem>
            <MenuItem value="EXPIRED">Expired Batches</MenuItem>
          </TextField>
          <Typography variant="body2" color="text.secondary">
            Total Batches: <strong>{totalElements}</strong>
          </Typography>
        </Stack>
      </Paper>

      {/* Batches Table */}
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Product Name</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Batch Number</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Expiry Date</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Available Qty</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Cost Price</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Selling Price</TableCell>
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
              ) : batches.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No inventory batches found.
                  </TableCell>
                </TableRow>
              ) : (
                batches.map((b) => {
                  let expiryChip = null;
                  if (b.expired) {
                    expiryChip = (
                      <Chip
                        icon={<XCircle size={14} />}
                        label={`Expired (${b.expiryDate})`}
                        color="error"
                        size="small"
                        sx={{ fontWeight: 700 }}
                      />
                    );
                  } else if (b.nearExpiry) {
                    expiryChip = (
                      <Chip
                        icon={<AlertTriangle size={14} />}
                        label={`Near Expiry (${b.expiryDate})`}
                        color="warning"
                        size="small"
                        sx={{ fontWeight: 700 }}
                      />
                    );
                  } else if (b.expiryDate) {
                    expiryChip = (
                      <Chip
                        icon={<CheckCircle2 size={14} />}
                        label={b.expiryDate}
                        color="success"
                        variant="outlined"
                        size="small"
                      />
                    );
                  } else {
                    expiryChip = <Typography variant="caption">N/A</Typography>;
                  }

                  return (
                    <TableRow key={b.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight={700}>
                          {b.productName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          SKU: {b.productSku} {b.productBarcode ? `• Barcode: ${b.productBarcode}` : ''}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={b.batchNumber}
                          size="small"
                          sx={{ fontFamily: 'monospace', fontWeight: 600 }}
                        />
                      </TableCell>
                      <TableCell align="center">{expiryChip}</TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" fontWeight={800} color={b.quantity > 0 ? 'text.primary' : 'error.main'}>
                          {b.quantity} {b.unitCode}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">₹{b.costPrice}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 700, color: 'primary.main' }}>
                        ₹{b.sellingPrice}
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          label={b.status}
                          size="small"
                          color={b.status === 'ACTIVE' ? 'primary' : b.status === 'EXPIRED' ? 'error' : 'default'}
                          variant="outlined"
                          sx={{ fontWeight: 600 }}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Button
                          size="small"
                          variant="outlined"
                          startIcon={<SlidersHorizontal size={14} />}
                          onClick={() => handleOpenAdjust(b)}
                          sx={{ textTransform: 'none', fontWeight: 600 }}
                        >
                          Adjust
                        </Button>
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

      {/* Stock Adjustment Dialog */}
      <Dialog
        open={adjustOpen}
        onClose={() => setAdjustOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          Manual Stock Adjustment • {selectedBatch?.productName}
        </DialogTitle>
        <DialogContent dividers>
          {adjustError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {adjustError}
            </Alert>
          )}

          {selectedBatch && (
            <Box sx={{ mb: 3, p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
              <Typography variant="body2">
                Batch: <strong>{selectedBatch.batchNumber}</strong> | Current Stock:{' '}
                <strong>
                  {selectedBatch.quantity} {selectedBatch.unitCode}
                </strong>
              </Typography>
            </Box>
          )}

          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField
                required
                type="number"
                fullWidth
                label="Adjustment Delta Quantity"
                value={adjustQuantity}
                onChange={(e) => setAdjustQuantity(Number(e.target.value))}
                helperText="Use negative (e.g. -2) to deduct, positive (e.g. 5) to add."
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                select
                required
                fullWidth
                label="Adjustment Reason"
                value={adjustReason}
                onChange={(e) => setAdjustReason(e.target.value as any)}
              >
                <MenuItem value="DAMAGED">Damaged Goods</MenuItem>
                <MenuItem value="EXPIRED">Expired Stock</MenuItem>
                <MenuItem value="THEFT_OR_LOST">Theft or Missing</MenuItem>
                <MenuItem value="COUNTING_ERROR">Stock Counting Correction</MenuItem>
                <MenuItem value="MANUAL_CORRECTION">Manual Balance Correction</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Notes / Audit Remarks"
                value={adjustNotes}
                onChange={(e) => setAdjustNotes(e.target.value)}
                placeholder="e.g. Found 2 packs punctured during shelf inspection"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setAdjustOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSaveAdjustment}
            disabled={submitting}
            sx={{ fontWeight: 700 }}
          >
            {submitting ? <CircularProgress size={20} /> : 'Save Audit Adjustment'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
