import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Stack,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  ListItemSecondaryAction,
  Chip,
  CircularProgress,
  Divider,
  Alert,
} from '@mui/material';
import {
  AlertTriangle,
  Clock,
  DollarSign,
  CheckCircle,
  Bell,
  CheckCheck,
  RefreshCw,
} from 'lucide-react';
import { notificationService } from '../services/api';
import { Notification } from '../types';

export const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const res = await notificationService.getAll({ size: 50 });
      // Safely handle both array and paged response
      const items = Array.isArray(res.data)
        ? res.data
        : ((res.data as any)?.content || []);
      setNotifications(items);
    } catch (e) {
      console.error('Failed to load notifications', e);
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSyncAlerts = async () => {
    try {
      setSyncing(true);
      await loadNotifications();
    } finally {
      setSyncing(false);
    }
  };

  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true, isRead: true } : n))
      );
    } catch (e) {
      console.error('Failed to mark read', e);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true, isRead: true })));
    } catch (e) {
      console.error('Failed to mark all read', e);
    }
  };

  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'OUT_OF_STOCK':
      case 'EXPIRED':
        return <AlertTriangle color="#ef4444" size={24} />;
      case 'LOW_STOCK':
      case 'NEAR_EXPIRY':
        return <Clock color="#f59e0b" size={24} />;
      case 'CUSTOMER_PAYMENT_DUE':
      case 'SUPPLIER_PAYMENT_DUE':
        return <DollarSign color="#0284c7" size={24} />;
      default:
        return <Bell color="#64748b" size={24} />;
    }
  };

  const getSeverityColor = (sev?: string) => {
    if (sev === 'CRITICAL') return 'error';
    if (sev === 'WARNING') return 'warning';
    return 'info';
  };

  const unreadCount = notifications.filter((n) => !n.read && !n.isRead).length;

  return (
    <Box sx={{ p: 3, maxWidth: 900, mx: 'auto' }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            Store Notifications & Inventory Alerts
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Critical stock thresholds, near-expiry warnings, and customer payment dues ({unreadCount} unread)
          </Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button
            variant="outlined"
            startIcon={<RefreshCw size={16} className={syncing ? 'animate-spin' : ''} />}
            onClick={handleSyncAlerts}
            disabled={syncing}
            sx={{ fontWeight: 600 }}
          >
            Sync Alerts
          </Button>
          <Button
            variant="contained"
            startIcon={<CheckCheck size={18} />}
            onClick={handleMarkAllRead}
            disabled={unreadCount === 0}
            sx={{ fontWeight: 600 }}
          >
            Mark All as Read
          </Button>
        </Stack>
      </Stack>

      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        {loading ? (
          <Box sx={{ py: 6, textAlign: 'center' }}>
            <CircularProgress />
          </Box>
        ) : notifications.length === 0 ? (
          <Box sx={{ py: 8, textAlign: 'center', color: 'text.secondary' }}>
            <CheckCircle size={48} color="#10b981" style={{ opacity: 0.8, marginBottom: 8 }} />
            <Typography variant="h6" fontWeight={700}>
              All Caught Up!
            </Typography>
            <Typography variant="body2">No active alerts or critical notifications at this time.</Typography>
          </Box>
        ) : (
          <List disablePadding>
            {notifications.map((n, idx) => {
              const isItemRead = Boolean(n.read || n.isRead);
              return (
                <React.Fragment key={n.id}>
                  <ListItem
                    sx={{
                      p: 2.5,
                      bgcolor: isItemRead ? 'transparent' : 'rgba(16, 185, 129, 0.05)',
                      borderLeft: isItemRead ? '4px solid transparent' : '4px solid #10b981',
                      transition: 'background-color 0.2s',
                      '&:hover': { bgcolor: 'action.hover' },
                    }}
                  >
                    <ListItemIcon sx={{ minWidth: 46 }}>{getAlertIcon(n.type)}</ListItemIcon>
                    <ListItemText
                      primary={
                        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
                          <Typography
                            variant="subtitle2"
                            fontWeight={isItemRead ? 600 : 800}
                            color={isItemRead ? 'text.secondary' : 'text.primary'}
                          >
                            {n.title}
                          </Typography>
                          <Chip
                            size="small"
                            label={n.severity || 'INFO'}
                            color={getSeverityColor(n.severity) as any}
                            sx={{ fontSize: '0.65rem', height: 20, fontWeight: 700 }}
                          />
                          <Chip
                            size="small"
                            label={n.type.replace(/_/g, ' ')}
                            variant="outlined"
                            sx={{ fontSize: '0.65rem', height: 20 }}
                          />
                        </Stack>
                      }
                      secondary={
                        <Box sx={{ mt: 0.5 }}>
                          <Typography variant="body2" color="text.secondary">
                            {n.message}
                          </Typography>
                          <Typography variant="caption" color="text.disabled" sx={{ mt: 0.5, display: 'block' }}>
                            {n.createdAt ? new Date(n.createdAt).toLocaleString() : 'Recent'}
                          </Typography>
                        </Box>
                      }
                    />
                    {!isItemRead && (
                      <ListItemSecondaryAction>
                        <Button
                          size="small"
                          variant="text"
                          onClick={() => handleMarkAsRead(n.id)}
                          sx={{ fontWeight: 700 }}
                        >
                          Mark Read
                        </Button>
                      </ListItemSecondaryAction>
                    )}
                  </ListItem>
                  {idx < notifications.length - 1 && <Divider component="li" />}
                </React.Fragment>
              );
            })}
          </List>
        )}
      </Paper>
    </Box>
  );
};
