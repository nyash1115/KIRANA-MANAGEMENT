import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Stack,
  Button,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Grid,
  Card,
  CardContent,
  TextField,
  CircularProgress,
  Divider,
} from '@mui/material';
import { Download, Calendar, BarChart3, TrendingUp, ShieldCheck, Package } from 'lucide-react';
import { reportService } from '../services/api';
import { ProfitReport, GstSummaryReport } from '../types';

export const ReportsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [loading, setLoading] = useState(true);

  // Date Range (default: current month)
  const firstDayOfMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
    .toISOString()
    .split('T')[0];
  const todayStr = new Date().toISOString().split('T')[0];

  const [startDate, setStartDate] = useState(firstDayOfMonth);
  const [endDate, setEndDate] = useState(todayStr);

  const [profitData, setProfitData] = useState<ProfitReport | null>(null);
  const [gstData, setGstData] = useState<GstSummaryReport | null>(null);

  useEffect(() => {
    loadReports();
  }, [startDate, endDate]);

  const loadReports = async () => {
    try {
      setLoading(true);
      const [profitRes, gstRes] = await Promise.all([
        reportService.getProfitReport(startDate, endDate),
        reportService.getGstReport(startDate, endDate),
      ]);
      setProfitData(profitRes.data);
      setGstData(gstRes.data);
    } catch (e) {
      console.error('Failed to load reports', e);
    } finally {
      setLoading(false);
    }
  };

  const handleExportCsv = () => {
    if (!profitData && !gstData) return;
    let csvContent = 'data:text/csv;charset=utf-8,';

    if (activeTab === 0 && profitData) {
      csvContent += 'Financial Profit & Loss Report\n';
      csvContent += `Period,${startDate} to ${endDate}\n\n`;
      csvContent += 'Metric,Amount (INR)\n';
      csvContent += `Gross Sales,${profitData.grossSales}\n`;
      csvContent += `Discounts,${profitData.totalDiscounts}\n`;
      csvContent += `Net Sales,${profitData.netSales}\n`;
      csvContent += `Cost of Goods Sold (COGS),${profitData.costOfGoodsSold}\n`;
      csvContent += `Gross Profit,${profitData.grossProfit}\n`;
      csvContent += `Gross Margin,${profitData.profitMarginPercentage}%\n`;
    } else if (activeTab === 1 && gstData) {
      csvContent += 'GSTR-1 Tax Summary Report\n';
      csvContent += `Period,${startDate} to ${endDate}\n\n`;
      csvContent += 'GST Rate Slab,Taxable Value,CGST,SGST,IGST,Total Tax\n';
      gstData.slabs.forEach((s) => {
        csvContent += `${s.rate}%,${s.taxableAmount},${s.cgst},${s.sgst},${s.igst},${s.totalTax}\n`;
      });
      csvContent += `Total,${gstData.totalTaxableAmount},${gstData.totalCgst},${gstData.totalSgst},${gstData.totalIgst},${gstData.totalTaxAmount}\n`;
    }

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `KiranaPro_Report_${startDate}_${endDate}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            Financial & Tax Reports
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gross profit (COGS basis), sales revenue, and GSTR-1 compliant tax slab calculations
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<Download size={18} />}
          onClick={handleExportCsv}
          sx={{ fontWeight: 700 }}
        >
          Export CSV / Excel
        </Button>
      </Stack>

      {/* Date Controls & Tab Bar */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={4}>
            <TextField
              type="date"
              size="small"
              fullWidth
              label="From Date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              type="date"
              size="small"
              fullWidth
              label="To Date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Button
              variant="contained"
              fullWidth
              onClick={loadReports}
              sx={{ height: 40, fontWeight: 700 }}
            >
              Update Report
            </Button>
          </Grid>
        </Grid>
      </Paper>

      {/* Report Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={activeTab} onChange={(_, val) => setActiveTab(val)}>
          <Tab
            icon={<TrendingUp size={18} />}
            iconPosition="start"
            label="Profit & Loss (P&L)"
            sx={{ fontWeight: 700 }}
          />
          <Tab
            icon={<ShieldCheck size={18} />}
            iconPosition="start"
            label="GSTR-1 Tax Summary"
            sx={{ fontWeight: 700 }}
          />
        </Tabs>
      </Box>

      {loading ? (
        <Box sx={{ py: 8, textAlign: 'center' }}>
          <CircularProgress />
        </Box>
      ) : activeTab === 0 ? (
        /* P&L View */
        <Box>
          <Grid container spacing={3} sx={{ mb: 3 }}>
            <Grid item xs={12} sm={6} md={3}>
              <Card elevation={2} sx={{ borderRadius: 3, borderLeft: '5px solid #10b981' }}>
                <CardContent sx={{ p: 2 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    NET SALES REVENUE
                  </Typography>
                  <Typography variant="h5" fontWeight={800} sx={{ mt: 0.5 }}>
                    ₹{profitData?.netSales.toLocaleString('en-IN') || 0}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    After ₹{profitData?.totalDiscounts || 0} discounts
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card elevation={2} sx={{ borderRadius: 3, borderLeft: '5px solid #ef4444' }}>
                <CardContent sx={{ p: 2 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    COST OF GOODS SOLD (COGS)
                  </Typography>
                  <Typography variant="h5" fontWeight={800} color="error.main" sx={{ mt: 0.5 }}>
                    ₹{profitData?.costOfGoodsSold.toLocaleString('en-IN') || 0}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Actual purchase batch basis
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card elevation={2} sx={{ borderRadius: 3, borderLeft: '5px solid #0284c7' }}>
                <CardContent sx={{ p: 2 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    GROSS PROFIT
                  </Typography>
                  <Typography variant="h5" fontWeight={800} color="#0284c7" sx={{ mt: 0.5 }}>
                    ₹{profitData?.grossProfit.toLocaleString('en-IN') || 0}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Net Sales minus COGS
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card elevation={2} sx={{ borderRadius: 3, borderLeft: '5px solid #8b5cf6' }}>
                <CardContent sx={{ p: 2 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    GROSS PROFIT MARGIN
                  </Typography>
                  <Typography variant="h5" fontWeight={800} color="#8b5cf6" sx={{ mt: 0.5 }}>
                    {profitData?.profitMarginPercentage || 0}%
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Percentage of net revenue
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          {/* Detailed P&L breakdown table */}
          <Paper elevation={2} sx={{ borderRadius: 3, p: 3 }}>
            <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
              Accounting Formula & Ledger Breakdown
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>Line Item</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Accounting Definition</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Amount (₹)</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 600 }}>Gross Sales</TableCell>
                    <TableCell color="text.secondary">Total invoice value before bill discounts</TableCell>
                    <TableCell align="right">₹{profitData?.grossSales}</TableCell>
                  </TableRow>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 600 }}>Less: Discounts Given</TableCell>
                    <TableCell color="text.secondary">Item and bill level discounts</TableCell>
                    <TableCell align="right" sx={{ color: 'error.main' }}>
                      - ₹{profitData?.totalDiscounts}
                    </TableCell>
                  </TableRow>
                  <TableRow hover sx={{ bgcolor: 'background.default' }}>
                    <TableCell sx={{ fontWeight: 700 }}>Net Sales Revenue</TableCell>
                    <TableCell>Gross Sales - Discounts</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800 }}>
                      ₹{profitData?.netSales}
                    </TableCell>
                  </TableRow>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 600 }}>Less: Cost of Goods Sold (COGS)</TableCell>
                    <TableCell color="text.secondary">Direct cost of inventory dispatched</TableCell>
                    <TableCell align="right" sx={{ color: 'error.main' }}>
                      - ₹{profitData?.costOfGoodsSold}
                    </TableCell>
                  </TableRow>
                  <TableRow hover sx={{ bgcolor: 'rgba(16, 185, 129, 0.08)' }}>
                    <TableCell sx={{ fontWeight: 800, color: 'primary.dark' }}>Gross Profit</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Net Sales - COGS</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 900, color: 'primary.dark', fontSize: '1.1rem' }}>
                      ₹{profitData?.grossProfit}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Box>
      ) : (
        /* GSTR-1 View */
        <Box>
          <Paper elevation={2} sx={{ borderRadius: 3, p: 3 }}>
            <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
              GSTR-1 Tax Slab Summary
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>GST Rate Slab</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Taxable Turnover (₹)</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>CGST (₹)</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>SGST (₹)</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>IGST (₹)</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Total Tax (₹)</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {gstData?.slabs.map((slab) => (
                    <TableRow key={slab.rate} hover>
                      <TableCell sx={{ fontWeight: 700 }}>{slab.rate}% Slab</TableCell>
                      <TableCell align="right">₹{slab.taxableAmount.toLocaleString('en-IN')}</TableCell>
                      <TableCell align="right">₹{slab.cgst.toLocaleString('en-IN')}</TableCell>
                      <TableCell align="right">₹{slab.sgst.toLocaleString('en-IN')}</TableCell>
                      <TableCell align="right">₹{slab.igst.toLocaleString('en-IN')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 700, color: 'primary.main' }}>
                        ₹{slab.totalTax.toLocaleString('en-IN')}
                      </TableCell>
                    </TableRow>
                  ))}
                  <TableRow sx={{ bgcolor: 'background.default' }}>
                    <TableCell sx={{ fontWeight: 800 }}>Total All Slabs</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800 }}>
                      ₹{gstData?.totalTaxableAmount.toLocaleString('en-IN')}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800 }}>
                      ₹{gstData?.totalCgst.toLocaleString('en-IN')}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800 }}>
                      ₹{gstData?.totalSgst.toLocaleString('en-IN')}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 800 }}>
                      ₹{gstData?.totalIgst.toLocaleString('en-IN')}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 900, color: 'primary.dark' }}>
                      ₹{gstData?.totalTaxAmount.toLocaleString('en-IN')}
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Box>
      )}
    </Box>
  );
};
