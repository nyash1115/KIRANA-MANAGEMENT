import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  InputAdornment,
  IconButton,
  Alert,
  CircularProgress,
  Stack,
  Chip,
  Divider,
} from '@mui/material';
import { Eye, EyeOff, Lock, User, ShoppingBag, ShieldCheck } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please provide both username and password.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await login(username, password);
      navigate('/pos');
    } catch (err: any) {
      console.error(err);
      setError(err?.response?.data?.message || 'Invalid username or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (u: string, p: string) => {
    setUsername(u);
    setPassword(p);
    setError(null);
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #064e3b 0%, #065f46 45%, #047857 100%)',
        p: 2,
      }}
    >
      <Card
        elevation={12}
        sx={{
          maxWidth: 440,
          width: '100%',
          borderRadius: 4,
          overflow: 'hidden',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.35)',
        }}
      >
        <Box
          sx={{
            bgcolor: 'primary.main',
            color: 'white',
            p: 4,
            textAlign: 'center',
            position: 'relative',
          }}
        >
          <Box
            sx={{
              display: 'inline-flex',
              p: 1.5,
              borderRadius: 3,
              bgcolor: 'rgba(255, 255, 255, 0.15)',
              mb: 1.5,
            }}
          >
            <ShoppingBag size={38} color="#fff" />
          </Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            KiranaPro POS
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.9, mt: 0.5 }}>
            Enterprise Retail & Billing Suite
          </Typography>
        </Box>

        <CardContent sx={{ p: 4 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }} onClose={() => setError(null)}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} noValidate>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <User size={18} color="#64748b" />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type={showPassword ? 'text' : 'password'}
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Lock size={18} color="#64748b" />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                    >
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                py: 1.5,
                fontWeight: 700,
                fontSize: '1rem',
                borderRadius: 2,
                boxShadow: '0 4px 12px rgba(16, 185, 129, 0.4)',
              }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In to Store'}
            </Button>
          </Box>

          <Divider sx={{ my: 3 }}>
            <Typography variant="caption" color="text.secondary" fontWeight={600}>
              QUICK DEMO ROLES
            </Typography>
          </Divider>

          <Stack direction="row" spacing={1} justifyContent="center">
            <Chip
              icon={<ShieldCheck size={14} />}
              label="Admin"
              color="primary"
              variant={username === 'admin' ? 'filled' : 'outlined'}
              onClick={() => handleQuickLogin('admin', 'admin123')}
              sx={{ cursor: 'pointer', fontWeight: 600 }}
            />
            <Chip
              label="Manager"
              color="info"
              variant={username === 'manager' ? 'filled' : 'outlined'}
              onClick={() => handleQuickLogin('manager', 'manager123')}
              sx={{ cursor: 'pointer', fontWeight: 600 }}
            />
            <Chip
              label="Cashier"
              color="secondary"
              variant={username === 'cashier' ? 'filled' : 'outlined'}
              onClick={() => handleQuickLogin('cashier', 'cashier123')}
              sx={{ cursor: 'pointer', fontWeight: 600 }}
            />
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
};
