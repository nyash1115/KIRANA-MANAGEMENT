import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Badge,
  Avatar,
  Chip,
  Tooltip,
  useTheme,
  useMediaQuery,
  Menu,
  MenuItem,
} from '@mui/material';
import {
  ShoppingCart,
  LayoutDashboard,
  Package,
  Layers,
  Truck,
  Receipt,
  BookOpen,
  Users,
  BarChart3,
  Bell,
  Settings,
  ShieldAlert,
  LogOut,
  Sun,
  Moon,
  Menu as MenuIcon,
  Store,
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useAppTheme } from '../../contexts/ThemeContext';
import api from '../../services/api';
import { ApiResponse, NotificationItem } from '../../types';

const DRAWER_WIDTH = 250;

export const AppLayout: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  const { user, logout, isAdmin, isManager } = useAuth();
  const { mode, toggleTheme } = useAppTheme();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const navigate = useNavigate();
  const location = useLocation();

  const [mobileOpen, setMobileOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notificationsAnchor, setNotificationsAnchor] = useState<null | HTMLElement>(null);
  const [recentNotifications, setRecentNotifications] = useState<NotificationItem[]>([]);

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchNotifications = async () => {
    try {
      const res = await api.get<ApiResponse<NotificationItem[]>>('/notifications/unread');
      if (res.data.success) {
        setRecentNotifications(res.data.data);
        setUnreadCount(res.data.data.length);
      }
    } catch {
      // ignore
    }
  };

  const navItems = [
    { label: 'POS Billing', path: '/pos', icon: <ShoppingCart size={20} />, primary: true },
    { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard size={20} /> },
    { label: 'Products', path: '/products', icon: <Package size={20} /> },
    { label: 'Inventory & FEFO', path: '/inventory', icon: <Layers size={20} /> },
    { label: 'Purchases', path: '/purchases', icon: <Truck size={20} />, hide: !isAdmin && !isManager },
    { label: 'Sales History', path: '/sales', icon: <Receipt size={20} /> },
    { label: 'Customer Khata', path: '/khata', icon: <BookOpen size={20} /> },
    { label: 'Suppliers', path: '/suppliers', icon: <Users size={20} />, hide: !isAdmin && !isManager },
    { label: 'Reports & GST', path: '/reports', icon: <BarChart3 size={20} />, hide: !isAdmin && !isManager },
    { label: 'Alerts', path: '/notifications', icon: <Bell size={20} />, badge: unreadCount },
    { label: 'Store Settings', path: '/settings', icon: <Settings size={20} /> },
    { label: 'Audit Logs', path: '/audit', icon: <ShieldAlert size={20} />, hide: !isAdmin },
  ].filter((item) => !item.hide);

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Brand Header */}
      <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <Box
          sx={{
            width: 42,
            height: 42,
            borderRadius: 2,
            backgroundColor: '#10B981',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            boxShadow: '0 4px 10px rgba(16, 185, 129, 0.3)',
          }}
        >
          <ShoppingCart size={24} />
        </Box>
        <Box>
          <Typography variant="h6" fontWeight={800} sx={{ letterSpacing: -0.5, lineHeight: 1.2 }}>
            Aapna Kirana
          </Typography>
          <Typography variant="caption" color="text.secondary" fontWeight={600}>
            POS &amp; Retail ERP
          </Typography>
        </Box>
      </Box>

      {/* Store Badge */}
      <Box sx={{ px: 2, mb: 1 }}>
        <Box
          sx={{
            p: 1.2,
            borderRadius: 2,
            backgroundColor: mode === 'dark' ? 'rgba(255,255,255,0.05)' : 'rgba(16, 185, 129, 0.08)',
            display: 'flex',
            alignItems: 'center',
            gap: 1,
          }}
        >
          <Store size={18} color="#10B981" />
          <Box sx={{ overflow: 'hidden' }}>
            <Typography variant="caption" fontWeight={700} display="block" noWrap>
              Branch 01 - Main Market
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ fontSize: '10px' }}>
              Pune • STR-001
            </Typography>
          </Box>
        </Box>
      </Box>

      <Divider sx={{ my: 0.5 }} />

      {/* Navigation Links */}
      <List sx={{ px: 1.5, flex: 1, py: 1 }}>
        {navItems.map((item) => {
          const isSelected = location.pathname === item.path;
          return (
            <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => {
                  navigate(item.path);
                  if (isMobile) setMobileOpen(false);
                }}
                sx={{
                  borderRadius: 2,
                  py: 1,
                  px: 1.5,
                  backgroundColor: isSelected
                    ? '#10B981'
                    : item.primary
                    ? mode === 'dark'
                      ? 'rgba(16, 185, 129, 0.15)'
                      : 'rgba(16, 185, 129, 0.1)'
                    : 'transparent',
                  color: isSelected ? '#FFFFFF' : item.primary ? '#10B981' : 'inherit',
                  fontWeight: isSelected || item.primary ? 700 : 500,
                  '&:hover': {
                    backgroundColor: isSelected
                      ? '#059669'
                      : mode === 'dark'
                      ? 'rgba(255,255,255,0.08)'
                      : 'rgba(16, 185, 129, 0.08)',
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 36,
                    color: isSelected ? '#FFFFFF' : item.primary ? '#10B981' : 'inherit',
                  }}
                >
                  {item.badge ? (
                    <Badge badgeContent={item.badge} color="error" max={99}>
                      {item.icon}
                    </Badge>
                  ) : (
                    item.icon
                  )}
                </ListItemIcon>
                <ListItemText
                  primary={item.label}
                  primaryTypographyProps={{
                    fontSize: '13.5px',
                    fontWeight: isSelected ? 700 : 500,
                  }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      <Divider />

      {/* User Session Bottom Bar */}
      <Box sx={{ p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, overflow: 'hidden' }}>
          <Avatar sx={{ width: 34, height: 34, bgcolor: '#10B981', fontSize: '14px', fontWeight: 'bold' }}>
            {user?.fullName?.charAt(0) || 'U'}
          </Avatar>
          <Box sx={{ overflow: 'hidden' }}>
            <Typography variant="body2" fontWeight={700} noWrap sx={{ fontSize: '13px' }}>
              {user?.fullName || 'User'}
            </Typography>
            <Chip
              label={user?.role?.replace('ROLE_', '')}
              size="small"
              sx={{
                height: 18,
                fontSize: '10px',
                fontWeight: 700,
                bgcolor:
                  user?.role === 'ROLE_ADMIN'
                    ? '#EF4444'
                    : user?.role === 'ROLE_MANAGER'
                    ? '#F59E0B'
                    : '#3B82F6',
                color: '#fff',
              }}
            />
          </Box>
        </Box>
        <Tooltip title="Logout">
          <IconButton onClick={logout} size="small" color="error">
            <LogOut size={18} />
          </IconButton>
        </Tooltip>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Top Navbar */}
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { md: `${DRAWER_WIDTH}px` },
          bgcolor: 'background.paper',
          color: 'text.primary',
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <IconButton
              color="inherit"
              edge="start"
              onClick={() => setMobileOpen(!mobileOpen)}
              sx={{ mr: 1, display: { md: 'none' } }}
            >
              <MenuIcon />
            </IconButton>
            <Typography variant="h6" fontWeight={700} sx={{ fontSize: { xs: '16px', sm: '18px' } }}>
              {navItems.find((n) => n.path === location.pathname)?.label || 'Kirana POS'}
            </Typography>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {/* Quick POS Button */}
            {location.pathname !== '/pos' && (
              <Chip
                icon={<ShoppingCart size={15} color="#fff" />}
                label="Open POS (F2)"
                onClick={() => navigate('/pos')}
                sx={{
                  bgcolor: '#10B981',
                  color: '#fff',
                  fontWeight: 700,
                  cursor: 'pointer',
                  '&:hover': { bgcolor: '#059669' },
                }}
              />
            )}

            {/* Notification Bell */}
            <Tooltip title="Alerts & Notifications">
              <IconButton onClick={(e) => setNotificationsAnchor(e.currentTarget)} color="inherit">
                <Badge badgeContent={unreadCount} color="error">
                  <Bell size={20} />
                </Badge>
              </IconButton>
            </Tooltip>

            {/* Notifications Dropdown Menu */}
            <Menu
              anchorEl={notificationsAnchor}
              open={Boolean(notificationsAnchor)}
              onClose={() => setNotificationsAnchor(null)}
              PaperProps={{ sx: { width: 340, maxHeight: 400, borderRadius: 2 } }}
            >
              <Box sx={{ p: 1.5, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="subtitle2" fontWeight={700}>
                  Unread Alerts ({unreadCount})
                </Typography>
                <Typography
                  variant="caption"
                  color="primary"
                  sx={{ cursor: 'pointer', fontWeight: 600 }}
                  onClick={() => {
                    navigate('/notifications');
                    setNotificationsAnchor(null);
                  }}
                >
                  View All
                </Typography>
              </Box>
              <Divider />
              {recentNotifications.length === 0 ? (
                <MenuItem disabled>
                  <Typography variant="body2" color="text.secondary">
                    No unread notifications
                  </Typography>
                </MenuItem>
              ) : (
                recentNotifications.slice(0, 5).map((n) => (
                  <MenuItem
                    key={n.id}
                    onClick={() => {
                      navigate('/notifications');
                      setNotificationsAnchor(null);
                    }}
                    sx={{ flexDirection: 'column', alignItems: 'flex-start', py: 1 }}
                  >
                    <Typography variant="body2" fontWeight={600} noWrap sx={{ width: '100%' }}>
                      {n.title}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" noWrap sx={{ width: '100%' }}>
                      {n.message}
                    </Typography>
                  </MenuItem>
                ))
              )}
            </Menu>

            {/* Theme Toggle */}
            <Tooltip title={mode === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}>
              <IconButton onClick={toggleTheme} color="inherit">
                {mode === 'dark' ? <Sun size={20} color="#F59E0B" /> : <Moon size={20} />}
              </IconButton>
            </Tooltip>
          </Box>
        </Toolbar>
      </AppBar>

      {/* Responsive Drawer */}
      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH },
          }}
        >
          {drawerContent}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: DRAWER_WIDTH,
              borderRight: 1,
              borderColor: 'divider',
            },
          }}
          open
        >
          {drawerContent}
        </Drawer>
      </Box>

      {/* Main Content Body */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: { xs: 2, sm: 3 },
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          mt: '64px',
          overflowX: 'hidden',
        }}
      >
        {children || <Outlet />}
      </Box>
    </Box>
  );
};
