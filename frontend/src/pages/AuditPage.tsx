import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  CircularProgress,
  TablePagination,
  TextField,
  MenuItem,
  Grid,
} from '@mui/material';
import { ShieldAlert, Search } from 'lucide-react';
import { auditService } from '../services/api';
import { AuditLog } from '../types';

export const AuditPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);
  const [totalElements, setTotalElements] = useState(0);
  const [entityFilter, setEntityFilter] = useState('');

  useEffect(() => {
    loadAuditLogs();
  }, [page, rowsPerPage, entityFilter]);

  const loadAuditLogs = async () => {
    try {
      setLoading(true);
      const res = await auditService.getAll({
        page,
        size: rowsPerPage,
        entityName: entityFilter || undefined,
      });
      setLogs(res.data.content);
      setTotalElements(res.data.totalElements);
    } catch (e) {
      console.error('Failed to load audit logs', e);
    } finally {
      setLoading(false);
    }
  };

  const getActionColor = (action: string) => {
    if (action.includes('CREATE') || action.includes('PURCHASE')) return 'success';
    if (action.includes('CANCEL') || action.includes('DELETE')) return 'error';
    if (action.includes('RETURN') || action.includes('ADJUST')) return 'warning';
    return 'primary';
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            System Security & Audit Trail
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Immutable log of sales, cancellations, manual stock adjustments, pricing modifications, and logins
          </Typography>
        </Box>
      </Stack>

      {/* Filter */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={4}>
            <TextField
              select
              size="small"
              fullWidth
              label="Filter by Entity"
              value={entityFilter}
              onChange={(e) => {
                setEntityFilter(e.target.value);
                setPage(0);
              }}
            >
              <MenuItem value="">All Entities</MenuItem>
              <MenuItem value="Sale">Sale / Billing</MenuItem>
              <MenuItem value="Purchase">Purchase Orders</MenuItem>
              <MenuItem value="Product">Products</MenuItem>
              <MenuItem value="InventoryBatch">Inventory Batches</MenuItem>
              <MenuItem value="StockAdjustment">Stock Adjustments</MenuItem>
              <MenuItem value="CustomerPayment">Customer Khata Payments</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={8} sx={{ textAlign: { sm: 'right' } }}>
            <Typography variant="body2" color="text.secondary">
              Total Audited Actions: <strong>{totalElements}</strong>
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* Audit Log Table */}
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Timestamp</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>User</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Action</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Entity</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Entity ID</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Audit Details / Changes</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={32} />
                  </TableCell>
                </TableRow>
              ) : logs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No audit records found.
                  </TableCell>
                </TableRow>
              ) : (
                logs.map((log) => (
                  <TableRow key={log.id} hover>
                    <TableCell sx={{ whiteSpace: 'nowrap' }}>
                      <Typography variant="body2">
                        {new Date(log.createdAt).toLocaleDateString()}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(log.createdAt).toLocaleTimeString()}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={700}>
                        {log.username || 'System'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={log.action}
                        color={getActionColor(log.action) as any}
                        sx={{ fontWeight: 700 }}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600}>
                        {log.entityName}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                        #{log.entityId}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography
                        variant="caption"
                        sx={{
                          fontFamily: 'monospace',
                          maxHeight: 48,
                          overflow: 'hidden',
                          display: 'block',
                          color: 'text.secondary',
                        }}
                      >
                        {log.newValue || log.oldValue || 'Audit trail recorded'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[15, 30, 50]}
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
    </Box>
  );
};
