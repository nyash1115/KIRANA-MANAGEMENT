import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Stack,
  Button,
  TextField,
  Grid,
  Divider,
  Alert,
  CircularProgress,
  FormControlLabel,
  Switch,
} from '@mui/material';
import { Save, Store, Receipt, Bell, ShieldCheck } from 'lucide-react';
import { settingsService } from '../services/api';
import { StoreSettings } from '../types';

export const SettingsPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [settings, setSettings] = useState<StoreSettings>({
    id: 1,
    storeId: 1,
    invoicePrefix: 'INV-',
    defaultGstType: 'EXCLUSIVE',
    enableNegativeStock: false,
    lowStockThreshold: 10,
    expiryWarningDays: 30,
    receiptFooterMessage: 'Thank you for shopping with us! Please visit again.',
    termsAndConditions: 'Goods once sold can only be returned within 3 days with original bill.',
  });

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      setLoading(true);
      const res = await settingsService.get();
      setSettings(res.data);
    } catch (e) {
      console.error('Failed to load settings', e);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError(null);
      setSuccess(false);
      await settingsService.update(settings);
      setSuccess(true);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to update store settings.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ py: 8, textAlign: 'center' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, maxWidth: 900, mx: 'auto' }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            Store & Tax Configurations
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Configure invoice numbering, thermal receipt footer, GST mode, and threshold alerts
          </Typography>
        </Box>
      </Stack>

      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess(false)}>
          Store settings updated successfully!
        </Alert>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <form onSubmit={handleSave}>
        {/* Invoice & Billing Configurations */}
        <Paper elevation={2} sx={{ p: 3, borderRadius: 3, mb: 3 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
            <Receipt size={20} color="#10b981" />
            <Typography variant="h6" fontWeight={700}>
              Invoice & Thermal Receipt Settings
            </Typography>
          </Stack>
          <Divider sx={{ mb: 2.5 }} />

          <Grid container spacing={2.5}>
            <Grid item xs={12} sm={6}>
              <TextField
                required
                fullWidth
                label="Invoice Number Prefix"
                value={settings.invoicePrefix}
                onChange={(e) => setSettings({ ...settings, invoicePrefix: e.target.value })}
                helperText="Prefix for generated bill numbers, e.g. INV- or STR1-"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                select
                fullWidth
                label="Default Tax Mode"
                value={settings.defaultGstType}
                onChange={(e) => setSettings({ ...settings, defaultGstType: e.target.value as any })}
                SelectProps={{ native: true }}
              >
                <option value="EXCLUSIVE">Tax Exclusive (GST added to base)</option>
                <option value="INCLUSIVE">Tax Inclusive (GST bundled in MRP)</option>
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Thermal Receipt Footer Greetings"
                value={settings.receiptFooterMessage}
                onChange={(e) => setSettings({ ...settings, receiptFooterMessage: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Invoice Terms & Return Conditions"
                value={settings.termsAndConditions}
                onChange={(e) => setSettings({ ...settings, termsAndConditions: e.target.value })}
              />
            </Grid>
          </Grid>
        </Paper>

        {/* Inventory & Alert Thresholds */}
        <Paper elevation={2} sx={{ p: 3, borderRadius: 3, mb: 3 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
            <Bell size={20} color="#f59e0b" />
            <Typography variant="h6" fontWeight={700}>
              Inventory Alert Thresholds
            </Typography>
          </Stack>
          <Divider sx={{ mb: 2.5 }} />

          <Grid container spacing={2.5}>
            <Grid item xs={12} sm={6}>
              <TextField
                required
                type="number"
                fullWidth
                label="Default Low Stock Threshold (Units)"
                value={settings.lowStockThreshold}
                onChange={(e) => setSettings({ ...settings, lowStockThreshold: Number(e.target.value) })}
                helperText="Trigger low-stock warning when quantity falls below this value"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                required
                type="number"
                fullWidth
                label="Near Expiry Alert Horizon (Days)"
                value={settings.expiryWarningDays}
                onChange={(e) => setSettings({ ...settings, expiryWarningDays: Number(e.target.value) })}
                helperText="Trigger near-expiry alert when batches expire within this many days"
              />
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={settings.enableNegativeStock}
                    onChange={(e) => setSettings({ ...settings, enableNegativeStock: e.target.checked })}
                    color="warning"
                  />
                }
                label="Allow Negative Stock Selling (Not recommended for accurate inventory auditing)"
              />
            </Grid>
          </Grid>
        </Paper>

        <Box sx={{ textAlign: 'right', mt: 2 }}>
          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={saving}
            startIcon={<Save size={18} />}
            sx={{ px: 4, fontWeight: 700 }}
          >
            {saving ? <CircularProgress size={22} color="inherit" /> : 'Save Settings'}
          </Button>
        </Box>
      </form>
    </Box>
  );
};
