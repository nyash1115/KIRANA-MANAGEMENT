import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  TextField,
  InputAdornment,
  Button,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Divider,
  Stack,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Tooltip,
} from '@mui/material';
import {
  Scan,
  Search,
  Plus,
  Minus,
  Trash2,
  Receipt,
  UserCheck,
  CreditCard,
  Banknote,
  QrCode,
  BookOpen,
  ArrowRight,
  ShoppingBag,
} from 'lucide-react';
import { productService, customerService, saleService, categoryService } from '../services/api';
import { Product, Customer, Category, Sale, PaymentRequest } from '../types';
import { PrintInvoiceModal } from '../components/common/PrintInvoiceModal';

interface CartItem {
  productId: number;
  productName: string;
  barcode?: string;
  sku: string;
  unitName: string;
  unitCode: string;
  quantity: number;
  unitPrice: number;
  costPrice: number;
  mrp: number;
  discountPercentage: number;
  gstRate: number;
  availableStock: number;
}

export const PosPage: React.FC = () => {
  // State
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState<number | ''>('');
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);

  // Cart
  const [cart, setCart] = useState<CartItem[]>([]);
  const [billDiscountType, setBillDiscountType] = useState<'PERCENTAGE' | 'FIXED'>('PERCENTAGE');
  const [billDiscountValue, setBillDiscountValue] = useState<number>(0);

  // Barcode / Search
  const [barcodeInput, setBarcodeInput] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [barcodeError, setBarcodeError] = useState<string | null>(null);
  const [scanSuccessMessage, setScanSuccessMessage] = useState<string | null>(null);

  // Payment Modal
  const [paymentModalOpen, setPaymentModalOpen] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'UPI' | 'CARD' | 'UDHAAR' | 'MIXED'>('CASH');
  const [cashAmount, setCashAmount] = useState<number>(0);
  const [upiAmount, setUpiAmount] = useState<number>(0);
  const [cardAmount, setCardAmount] = useState<number>(0);
  const [udhaarAmount, setUdhaarAmount] = useState<number>(0);
  const [tenderedCash, setTenderedCash] = useState<number>(0);
  const [submitting, setSubmitting] = useState(false);
  const [checkoutError, setCheckoutError] = useState<string | null>(null);

  // Invoice Print Modal
  const [completedSale, setCompletedSale] = useState<Sale | null>(null);
  const [printModalOpen, setPrintModalOpen] = useState(false);

  // Quick Add Customer Dialog
  const [customerModalOpen, setCustomerModalOpen] = useState(false);
  const [newCustName, setNewCustName] = useState('');
  const [newCustPhone, setNewCustPhone] = useState('');
  const [newCustCreditLimit, setNewCustCreditLimit] = useState('2000');

  const barcodeInputRef = useRef<HTMLInputElement>(null);

  // Initial Data
  useEffect(() => {
    loadCategories();
    loadCustomers();
    loadProducts();
  }, []);

  const loadCategories = async () => {
    try {
      const res = await categoryService.getAll();
      setCategories(res.data);
    } catch (e) {
      console.error('Failed to load categories', e);
    }
  };

  const loadCustomers = async () => {
    try {
      const res = await customerService.getAll({ size: 100 });
      setCustomers(res.data.content);
    } catch (e) {
      console.error('Failed to load customers', e);
    }
  };

  const loadProducts = async (catId?: number, search?: string) => {
    try {
      const res = await productService.getAll({
        categoryId: catId,
        search: search,
        size: 30,
      });
      setProducts(res.data.content);
    } catch (e) {
      console.error('Failed to load products', e);
    }
  };

  const handleCategoryFilter = (catId: number | null) => {
    setSelectedCategory(catId);
    loadProducts(catId || undefined, searchQuery);
  };

  const handleSearchFilter = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setSearchQuery(val);
    loadProducts(selectedCategory || undefined, val);
  };

  // Add Product to Cart helper
  const addProductToCart = (prod: Product) => {
    setCart((prev) => {
      const existing = prev.find((i) => i.productId === prod.id);
      if (existing) {
        return prev.map((item) =>
          item.productId === prod.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      } else {
        return [
          ...prev,
          {
            productId: prod.id,
            productName: prod.name,
            barcode: prod.barcode,
            sku: prod.sku,
            unitName: prod.unitName,
            unitCode: prod.unitCode,
            quantity: 1,
            unitPrice: Number(prod.defaultSellingPrice),
            costPrice: Number(prod.defaultCostPrice),
            mrp: Number(prod.defaultMrp),
            discountPercentage: 0,
            gstRate: Number(prod.gstRate),
            availableStock: prod.totalAvailableStock || 0,
          },
        ];
      }
    });
  };

  // Authentic POS Scanner Audio Beep Feedback (Web Audio API)
  const playScanBeep = (success: boolean) => {
    try {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain);
      gain.connect(ctx.destination);

      if (success) {
        osc.type = 'sine';
        osc.frequency.setValueAtTime(1760, ctx.currentTime); // High pitch supermarket beep
        gain.gain.setValueAtTime(0.25, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.08);
        osc.start();
        osc.stop(ctx.currentTime + 0.09);
      } else {
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(260, ctx.currentTime); // Low pitch error tone
        gain.gain.setValueAtTime(0.3, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.22);
        osc.start();
        osc.stop(ctx.currentTime + 0.23);
      }
    } catch {
      // Audio context might be restricted before user gesture
    }
  };

  // Robust Barcode & SKU Lookup Handler
  const processBarcodeScan = async (rawCode: string) => {
    const code = rawCode.trim();
    if (!code) return;

    setBarcodeError(null);
    setScanSuccessMessage(null);

    // 1. Check in already loaded products state (instant response)
    const localMatch = products.find(
      (p) =>
        (p.barcode && p.barcode.toLowerCase() === code.toLowerCase()) ||
        (p.sku && p.sku.toLowerCase() === code.toLowerCase())
    );
    if (localMatch) {
      addProductToCart(localMatch);
      playScanBeep(true);
      setScanSuccessMessage(`Scanned: ${localMatch.name}`);
      setBarcodeInput('');
      setTimeout(() => setScanSuccessMessage(null), 3000);
      barcodeInputRef.current?.focus();
      return;
    }

    // 2. Query backend by barcode endpoint
    try {
      const res = await productService.getByBarcode(code);
      if (res && res.data) {
        addProductToCart(res.data);
        playScanBeep(true);
        setScanSuccessMessage(`Scanned: ${res.data.name}`);
        setBarcodeInput('');
        setTimeout(() => setScanSuccessMessage(null), 3000);
        barcodeInputRef.current?.focus();
        return;
      }
    } catch (err: any) {
      console.warn('Barcode not found directly, trying SKU/search fallback...', err);
    }

    // 3. Fallback: Search endpoint (matches SKU, partial barcode, or name)
    try {
      const searchRes = await productService.getAll({ search: code, size: 5 });
      const foundList = searchRes.data?.content || [];
      if (foundList.length > 0) {
        const item = foundList[0];
        addProductToCart(item);
        playScanBeep(true);
        setScanSuccessMessage(`Matched: ${item.name}`);
        setBarcodeInput('');
        setTimeout(() => setScanSuccessMessage(null), 3000);
        barcodeInputRef.current?.focus();
        return;
      }
    } catch (searchErr) {
      console.error('Fallback search failed', searchErr);
    }

    // 4. Not found in catalog
    playScanBeep(false);
    setBarcodeError(`Barcode / SKU "${code}" not found in inventory.`);
    setBarcodeInput('');
    barcodeInputRef.current?.focus();
  };

  // Form submit handler
  const handleBarcodeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!barcodeInput.trim()) return;
    await processBarcodeScan(barcodeInput);
  };

  // Global Hardware USB Barcode Scanner Key Listener
  // Hardware scanners type very quickly (interval < 50ms) followed by 'Enter'
  useEffect(() => {
    let scanBuffer = '';
    let lastKeyTime = Date.now();

    const handleGlobalKeyDown = (e: KeyboardEvent) => {
      // Don't intercept when user is typing in dialogs or search boxes
      const target = e.target as HTMLElement | null;
      const isInput = target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA');
      const isBarcodeInput = target === barcodeInputRef.current;

      const currentTime = Date.now();
      const timeDiff = currentTime - lastKeyTime;
      lastKeyTime = currentTime;

      if (e.key === 'Enter') {
        if (scanBuffer.length >= 3 && timeDiff < 100) {
          e.preventDefault();
          processBarcodeScan(scanBuffer);
          scanBuffer = '';
          return;
        }
        scanBuffer = '';
        return;
      }

      if (e.key.length > 1) return; // Ignore Shift, Alt, etc.

      // Rapid typing (< 65ms between keys) or focused in barcode input
      if (timeDiff < 65 || isBarcodeInput) {
        scanBuffer += e.key;
      } else if (!isInput) {
        // Cashier clicked elsewhere on POS screen, begin accumulating buffer
        scanBuffer = e.key;
      }
    };

    window.addEventListener('keydown', handleGlobalKeyDown);
    return () => window.removeEventListener('keydown', handleGlobalKeyDown);
  }, [products]);

  const updateQuantity = (productId: number, delta: number) => {
    setCart((prev) =>
      prev
        .map((item) => {
          if (item.productId === productId) {
            const newQty = item.quantity + delta;
            return newQty > 0 ? { ...item, quantity: Number(newQty.toFixed(2)) } : null;
          }
          return item;
        })
        .filter(Boolean) as CartItem[]
    );
  };

  const setDirectQuantity = (productId: number, qty: number) => {
    if (qty <= 0) {
      removeFromCart(productId);
      return;
    }
    setCart((prev) =>
      prev.map((item) =>
        item.productId === productId ? { ...item, quantity: qty } : item
      )
    );
  };

  const removeFromCart = (productId: number) => {
    setCart((prev) => prev.filter((i) => i.productId !== productId));
  };

  const clearCart = () => {
    setCart([]);
    setBillDiscountValue(0);
    setSelectedCustomerId('');
    setSelectedCustomer(null);
  };

  // Calculations
  const calculateCartSummary = () => {
    let subtotal = 0;
    let itemDiscountTotal = 0;
    let taxableAmountTotal = 0;
    let cgstTotal = 0;
    let sgstTotal = 0;
    let igstTotal = 0;

    cart.forEach((item) => {
      const lineSubtotal = item.quantity * item.unitPrice;
      const lineDisc = (lineSubtotal * item.discountPercentage) / 100;
      const lineTaxable = lineSubtotal - lineDisc;

      const halfRate = item.gstRate / 2;
      const lineCgst = (lineTaxable * halfRate) / 100;
      const lineSgst = (lineTaxable * halfRate) / 100;

      subtotal += lineSubtotal;
      itemDiscountTotal += lineDisc;
      taxableAmountTotal += lineTaxable;
      cgstTotal += lineCgst;
      sgstTotal += lineSgst;
    });

    // Bill level discount
    let billDiscount = 0;
    if (billDiscountType === 'PERCENTAGE') {
      billDiscount = (taxableAmountTotal * billDiscountValue) / 100;
    } else {
      billDiscount = billDiscountValue;
    }

    const netTaxable = Math.max(0, taxableAmountTotal - billDiscount);
    // Recalculate taxes proportionally after bill discount if applied
    const discountRatio = taxableAmountTotal > 0 ? netTaxable / taxableAmountTotal : 1;
    const finalCgst = cgstTotal * discountRatio;
    const finalSgst = sgstTotal * discountRatio;
    const totalGst = finalCgst + finalSgst + igstTotal;

    const unroundedTotal = netTaxable + totalGst;
    const grandTotal = Math.round(unroundedTotal);
    const roundOff = Number((grandTotal - unroundedTotal).toFixed(2));

    return {
      subtotal: Number(subtotal.toFixed(2)),
      itemDiscountTotal: Number(itemDiscountTotal.toFixed(2)),
      billDiscount: Number(billDiscount.toFixed(2)),
      totalDiscount: Number((itemDiscountTotal + billDiscount).toFixed(2)),
      taxableAmount: Number(netTaxable.toFixed(2)),
      cgst: Number(finalCgst.toFixed(2)),
      sgst: Number(finalSgst.toFixed(2)),
      igst: Number(igstTotal.toFixed(2)),
      totalTax: Number(totalGst.toFixed(2)),
      roundOff,
      grandTotal,
    };
  };

  const summary = calculateCartSummary();

  // Customer selection
  const handleCustomerChange = (custId: number | '') => {
    setSelectedCustomerId(custId);
    if (!custId) {
      setSelectedCustomer(null);
    } else {
      const found = customers.find((c) => c.id === custId) || null;
      setSelectedCustomer(found);
    }
  };

  const handleCreateCustomer = async () => {
    if (!newCustName || !newCustPhone) return;
    try {
      const res = await customerService.create({
        name: newCustName,
        phone: newCustPhone,
        creditLimit: Number(newCustCreditLimit) || 2000,
        active: true,
      });
      setCustomers((prev) => [res.data, ...prev]);
      setSelectedCustomerId(res.data.id);
      setSelectedCustomer(res.data);
      setCustomerModalOpen(false);
      setNewCustName('');
      setNewCustPhone('');
    } catch (e) {
      console.error('Error creating customer', e);
    }
  };

  // Open Checkout
  const handleOpenCheckout = () => {
    if (cart.length === 0) return;
    setCashAmount(summary.grandTotal);
    setTenderedCash(summary.grandTotal);
    setUpiAmount(0);
    setCardAmount(0);
    setUdhaarAmount(0);
    setPaymentMethod('CASH');
    setCheckoutError(null);
    setPaymentModalOpen(true);
  };

  // Quick switch of payment mode in checkout modal
  const handlePaymentMethodTab = (method: 'CASH' | 'UPI' | 'CARD' | 'UDHAAR' | 'MIXED') => {
    setPaymentMethod(method);
    if (method === 'CASH') {
      setCashAmount(summary.grandTotal);
      setTenderedCash(summary.grandTotal);
      setUpiAmount(0);
      setCardAmount(0);
      setUdhaarAmount(0);
    } else if (method === 'UPI') {
      setUpiAmount(summary.grandTotal);
      setCashAmount(0);
      setCardAmount(0);
      setUdhaarAmount(0);
    } else if (method === 'CARD') {
      setCardAmount(summary.grandTotal);
      setCashAmount(0);
      setUpiAmount(0);
      setUdhaarAmount(0);
    } else if (method === 'UDHAAR') {
      if (!selectedCustomerId) {
        setCheckoutError('Please select or register a customer for Udhaar / Khata credit.');
        return;
      }
      setUdhaarAmount(summary.grandTotal);
      setCashAmount(0);
      setUpiAmount(0);
      setCardAmount(0);
    }
  };

  const handleCompleteSale = async () => {
    try {
      setSubmitting(true);
      setCheckoutError(null);

      // Validate payments
      const payments: PaymentRequest[] = [];
      if (paymentMethod === 'CASH' && cashAmount > 0) {
        payments.push({ paymentMethod: 'CASH', amount: cashAmount });
      } else if (paymentMethod === 'UPI' && upiAmount > 0) {
        payments.push({ paymentMethod: 'UPI', amount: upiAmount, transactionReference: 'UPI-' + Date.now().toString().slice(-6) });
      } else if (paymentMethod === 'CARD' && cardAmount > 0) {
        payments.push({ paymentMethod: 'CARD', amount: cardAmount, transactionReference: 'CARD-' + Date.now().toString().slice(-4) });
      } else if (paymentMethod === 'UDHAAR' && udhaarAmount > 0) {
        if (!selectedCustomerId) {
          setCheckoutError('Udhaar sale requires a registered customer.');
          setSubmitting(false);
          return;
        }
        payments.push({ paymentMethod: 'UDHAAR', amount: udhaarAmount });
      } else if (paymentMethod === 'MIXED') {
        const totalEntered = cashAmount + upiAmount + cardAmount + udhaarAmount;
        if (Math.abs(totalEntered - summary.grandTotal) > 0.05) {
          setCheckoutError(`Split payments sum (₹${totalEntered}) must equal Grand Total (₹${summary.grandTotal}).`);
          setSubmitting(false);
          return;
        }
        if (cashAmount > 0) payments.push({ paymentMethod: 'CASH', amount: cashAmount });
        if (upiAmount > 0) payments.push({ paymentMethod: 'UPI', amount: upiAmount });
        if (cardAmount > 0) payments.push({ paymentMethod: 'CARD', amount: cardAmount });
        if (udhaarAmount > 0) {
          if (!selectedCustomerId) {
            setCheckoutError('Udhaar portion requires a registered customer.');
            setSubmitting(false);
            return;
          }
          payments.push({ paymentMethod: 'UDHAAR', amount: udhaarAmount });
        }
      }

      const salePayload = {
        storeId: 1,
        customerId: selectedCustomerId ? Number(selectedCustomerId) : undefined,
        items: cart.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
          discountPercentage: i.discountPercentage,
        })),
        billDiscountType: billDiscountValue > 0 ? billDiscountType : undefined,
        billDiscountValue: billDiscountValue > 0 ? billDiscountValue : undefined,
        payments: payments,
        notes: 'POS Checkout',
      };

      const res = await saleService.create(salePayload);
      setCompletedSale(res.data);
      setPaymentModalOpen(false);
      setPrintModalOpen(true);
      clearCart();
    } catch (err: any) {
      console.error('Checkout error:', err);
      setCheckoutError(err?.response?.data?.message || 'Transaction failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const changeDue = Math.max(0, tenderedCash - cashAmount);

  return (
    <Box sx={{ p: 2 }}>
      <Grid container spacing={2}>
        {/* Left Column: Barcode Scan, Categories & Quick Products */}
        <Grid item xs={12} lg={7}>
          {/* Barcode scanner input */}
          <Paper
            elevation={2}
            sx={{
              p: 2,
              mb: 2,
              borderRadius: 3,
              bgcolor: 'background.paper',
              border: '2px solid',
              borderColor: 'primary.light',
            }}
          >
            {/* Quick Demo Barcode Chips */}
            <Box sx={{ mb: 1.5 }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary', display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.75 }}>
                <Scan size={14} color="#10b981" /> Quick Barcode Scan Demo (Click any to test scanner beep & instant add):
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
                {[
                  { name: 'Maggi (₹13)', code: '8901058852300' },
                  { name: 'Amul Butter (₹265)', code: '8901262010053' },
                  { name: 'Tata Salt (₹26)', code: '8901058852212' },
                  { name: 'Aashirvaad Atta (₹245)', code: '8901030012345' },
                  { name: 'Dettol Soap (₹50)', code: '8901396001017' },
                  { name: 'Fortune Oil (₹152)', code: '8906007281014' },
                ].map((item) => (
                  <Chip
                    key={item.code}
                    label={`${item.name}`}
                    size="small"
                    onClick={() => processBarcodeScan(item.code)}
                    color="primary"
                    variant="outlined"
                    clickable
                    sx={{
                      fontSize: '11px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      borderRadius: 1.5,
                      '&:hover': { bgcolor: 'primary.main', color: '#fff' },
                    }}
                  />
                ))}
              </Box>
            </Box>

            <form onSubmit={handleBarcodeSubmit}>
              <Stack direction="row" spacing={1} alignItems="center">
                <TextField
                  fullWidth
                  inputRef={barcodeInputRef}
                  placeholder="Scan product barcode (or type barcode/SKU & Enter)..."
                  value={barcodeInput}
                  onChange={(e) => setBarcodeInput(e.target.value)}
                  autoFocus
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Scan size={24} color="#10b981" />
                      </InputAdornment>
                    ),
                  }}
                  size="small"
                />
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  sx={{ px: 3, height: 40, fontWeight: 700 }}
                >
                  Scan
                </Button>
              </Stack>
            </form>

            {scanSuccessMessage && (
              <Alert severity="success" sx={{ mt: 1, py: 0.5, fontSize: '13px', fontWeight: 600 }}>
                {scanSuccessMessage}
              </Alert>
            )}

            {barcodeError && (
              <Alert
                severity="error"
                sx={{ mt: 1, py: 0.5, fontSize: '13px' }}
                action={
                  <Button color="inherit" size="small" onClick={() => setBarcodeError(null)}>
                    Dismiss
                  </Button>
                }
              >
                {barcodeError}
              </Alert>
            )}
          </Paper>

          {/* Product Category Pills & Search */}
          <Paper elevation={1} sx={{ p: 2, mb: 2, borderRadius: 3 }}>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1.5 }}>
              <TextField
                fullWidth
                size="small"
                placeholder="Search products by name or brand..."
                value={searchQuery}
                onChange={handleSearchFilter}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search size={18} color="#64748b" />
                    </InputAdornment>
                  ),
                }}
              />
            </Stack>

            <Box sx={{ display: 'flex', gap: 1, overflowX: 'auto', pb: 0.5 }}>
              <Chip
                label="All Categories"
                clickable
                color={selectedCategory === null ? 'primary' : 'default'}
                variant={selectedCategory === null ? 'filled' : 'outlined'}
                onClick={() => handleCategoryFilter(null)}
                sx={{ fontWeight: 600 }}
              />
              {categories.map((c) => (
                <Chip
                  key={c.id}
                  label={c.name}
                  clickable
                  color={selectedCategory === c.id ? 'primary' : 'default'}
                  variant={selectedCategory === c.id ? 'filled' : 'outlined'}
                  onClick={() => handleCategoryFilter(c.id)}
                  sx={{ fontWeight: 600 }}
                />
              ))}
            </Box>
          </Paper>

          {/* Quick Select Product Grid */}
          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
              gap: 1.5,
              maxHeight: 'calc(100vh - 330px)',
              overflowY: 'auto',
              pr: 0.5,
            }}
          >
            {products.map((prod) => (
              <Card
                key={prod.id}
                onClick={() => addProductToCart(prod)}
                elevation={1}
                sx={{
                  borderRadius: 3,
                  cursor: 'pointer',
                  border: '1px solid',
                  borderColor: 'divider',
                  transition: 'transform 0.15s ease, box-shadow 0.15s ease',
                  '&:hover': {
                    transform: 'translateY(-2px)',
                    boxShadow: 4,
                    borderColor: 'primary.main',
                  },
                }}
              >
                <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 } }}>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ textTransform: 'uppercase', fontWeight: 600, fontSize: '0.65rem' }}
                  >
                    {prod.categoryName}
                  </Typography>
                  <Typography
                    variant="body2"
                    fontWeight={700}
                    sx={{
                      lineHeight: 1.2,
                      my: 0.5,
                      height: 34,
                      overflow: 'hidden',
                      display: '-webkit-box',
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: 'vertical',
                    }}
                  >
                    {prod.name}
                  </Typography>
                  <Stack direction="row" justifyContent="space-between" alignItems="baseline" mt={1}>
                    <Typography variant="body1" fontWeight={800} color="primary.main">
                      ₹{prod.defaultSellingPrice}
                    </Typography>
                    <Typography
                      variant="caption"
                      sx={{ textDecoration: 'line-through', color: 'text.secondary' }}
                    >
                      ₹{prod.defaultMrp}
                    </Typography>
                  </Stack>
                  <Stack direction="row" justifyContent="space-between" alignItems="center" mt={0.5}>
                    <Chip
                      size="small"
                      label={`GST ${prod.gstRate}%`}
                      sx={{ height: 18, fontSize: '0.65rem' }}
                    />
                    <Typography variant="caption" color="text.secondary">
                      {prod.unitCode}
                    </Typography>
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Box>
        </Grid>

        {/* Right Column: POS Cart & Checkout */}
        <Grid item xs={12} lg={5}>
          <Paper
            elevation={3}
            sx={{
              p: 2,
              borderRadius: 3,
              display: 'flex',
              flexDirection: 'column',
              height: 'calc(100vh - 120px)',
              bgcolor: 'background.paper',
            }}
          >
            {/* Customer Selector Bar */}
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
              <FormControl size="small" fullWidth>
                <InputLabel id="cust-select-label">Customer (Khata / Udhaar)</InputLabel>
                <Select
                  labelId="cust-select-label"
                  value={selectedCustomerId}
                  label="Customer (Khata / Udhaar)"
                  onChange={(e) => handleCustomerChange(e.target.value as any)}
                >
                  <MenuItem value="">
                    <em>Walk-in Customer (Cash / UPI)</em>
                  </MenuItem>
                  {customers.map((c) => (
                    <MenuItem key={c.id} value={c.id}>
                      {c.name} ({c.phone}) - Khata: ₹{c.outstandingBalance}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Tooltip title="Quick Register Customer">
                <IconButton
                  color="primary"
                  onClick={() => setCustomerModalOpen(true)}
                  sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}
                >
                  <UserCheck size={20} />
                </IconButton>
              </Tooltip>
            </Stack>

            {selectedCustomer && (
              <Box
                sx={{
                  bgcolor: selectedCustomer.outstandingBalance > 0 ? 'warning.light' : 'success.light',
                  color: selectedCustomer.outstandingBalance > 0 ? 'warning.contrastText' : 'success.contrastText',
                  p: 1,
                  borderRadius: 2,
                  mb: 1.5,
                  fontSize: '0.8rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <span>
                  <strong>{selectedCustomer.name}</strong> • Khata Due: ₹{selectedCustomer.outstandingBalance}
                </span>
                <span>Limit: ₹{selectedCustomer.creditLimit}</span>
              </Box>
            )}

            {/* Cart Items Table */}
            <TableContainer sx={{ flex: 1, overflowY: 'auto' }}>
              <Table size="small" stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>Item</TableCell>
                    <TableCell align="center" sx={{ fontWeight: 700 }}>Qty</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Price</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 700 }}>Total</TableCell>
                    <TableCell align="center" sx={{ fontWeight: 700 }}></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {cart.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                        <ShoppingBag size={40} style={{ opacity: 0.3, marginBottom: 8 }} />
                        <Typography variant="body2">Cart is empty.</Typography>
                        <Typography variant="caption">Scan a barcode or click items to add.</Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    cart.map((item) => {
                      const lineTotal = item.quantity * item.unitPrice * (1 - item.discountPercentage / 100);
                      return (
                        <TableRow key={item.productId} hover>
                          <TableCell sx={{ py: 1 }}>
                            <Typography variant="body2" fontWeight={600}>
                              {item.productName}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              ₹{item.unitPrice} / {item.unitCode} • GST {item.gstRate}%
                            </Typography>
                          </TableCell>
                          <TableCell align="center" sx={{ py: 1, whiteSpace: 'nowrap' }}>
                            <Stack direction="row" alignItems="center" justifyContent="center" spacing={0.5}>
                              <IconButton
                                size="small"
                                onClick={() => updateQuantity(item.productId, -1)}
                                sx={{ p: 0.5 }}
                              >
                                <Minus size={14} />
                              </IconButton>
                              <Typography variant="body2" fontWeight={700} sx={{ minWidth: 24, textAlign: 'center' }}>
                                {item.quantity}
                              </Typography>
                              <IconButton
                                size="small"
                                onClick={() => updateQuantity(item.productId, 1)}
                                sx={{ p: 0.5 }}
                              >
                                <Plus size={14} />
                              </IconButton>
                            </Stack>
                          </TableCell>
                          <TableCell align="right" sx={{ py: 1 }}>
                            ₹{item.unitPrice}
                          </TableCell>
                          <TableCell align="right" sx={{ py: 1, fontWeight: 700 }}>
                            ₹{lineTotal.toFixed(2)}
                          </TableCell>
                          <TableCell align="center" sx={{ py: 1 }}>
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => removeFromCart(item.productId)}
                            >
                              <Trash2 size={16} />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      );
                    })
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            <Divider sx={{ my: 1.5 }} />

            {/* Calculations & Discounts */}
            <Box sx={{ px: 1, mb: 1 }}>
              <Grid container spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <Grid item xs={6}>
                  <TextField
                    size="small"
                    fullWidth
                    label="Bill Discount (%)"
                    type="number"
                    value={billDiscountValue || ''}
                    onChange={(e) => setBillDiscountValue(Number(e.target.value) || 0)}
                    InputProps={{
                      endAdornment: <InputAdornment position="end">%</InputAdornment>,
                    }}
                  />
                </Grid>
                <Grid item xs={6}>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography variant="caption" color="text.secondary">
                      Subtotal:
                    </Typography>
                    <Typography variant="caption" fontWeight={600}>
                      ₹{summary.subtotal}
                    </Typography>
                  </Stack>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography variant="caption" color="text.secondary">
                      Taxes (GST):
                    </Typography>
                    <Typography variant="caption" fontWeight={600}>
                      ₹{summary.totalTax}
                    </Typography>
                  </Stack>
                  {summary.roundOff !== 0 && (
                    <Stack direction="row" justifyContent="space-between">
                      <Typography variant="caption" color="text.secondary">
                        Round Off:
                      </Typography>
                      <Typography variant="caption" fontWeight={600}>
                        ₹{summary.roundOff}
                      </Typography>
                    </Stack>
                  )}
                </Grid>
              </Grid>

              {/* Grand Total Bar */}
              <Box
                sx={{
                  bgcolor: 'primary.main',
                  color: 'white',
                  p: 2,
                  borderRadius: 2.5,
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  boxShadow: '0 4px 12px rgba(16, 185, 129, 0.35)',
                }}
              >
                <Box>
                  <Typography variant="caption" sx={{ opacity: 0.9 }}>
                    GRAND TOTAL ({cart.length} items)
                  </Typography>
                  <Typography variant="h4" fontWeight={900}>
                    ₹{summary.grandTotal}
                  </Typography>
                </Box>
                <Button
                  variant="contained"
                  size="large"
                  disabled={cart.length === 0}
                  onClick={handleOpenCheckout}
                  sx={{
                    bgcolor: 'white',
                    color: 'primary.dark',
                    fontWeight: 800,
                    px: 3,
                    py: 1.2,
                    fontSize: '1.05rem',
                    '&:hover': { bgcolor: '#f1f5f9' },
                  }}
                  endIcon={<ArrowRight size={20} />}
                >
                  Pay Now
                </Button>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Payment Checkout Modal */}
      <Dialog
        open={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800, pb: 1 }}>
          Checkout • Bill ₹{summary.grandTotal}
        </DialogTitle>
        <DialogContent dividers>
          {checkoutError && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setCheckoutError(null)}>
              {checkoutError}
            </Alert>
          )}

          {/* Payment Method Switcher Tabs */}
          <Stack direction="row" spacing={1} sx={{ mb: 3 }} justifyContent="center">
            <Button
              variant={paymentMethod === 'CASH' ? 'contained' : 'outlined'}
              startIcon={<Banknote size={18} />}
              onClick={() => handlePaymentMethodTab('CASH')}
              sx={{ fontWeight: 700 }}
            >
              Cash
            </Button>
            <Button
              variant={paymentMethod === 'UPI' ? 'contained' : 'outlined'}
              startIcon={<QrCode size={18} />}
              onClick={() => handlePaymentMethodTab('UPI')}
              sx={{ fontWeight: 700 }}
            >
              UPI QR
            </Button>
            <Button
              variant={paymentMethod === 'CARD' ? 'contained' : 'outlined'}
              startIcon={<CreditCard size={18} />}
              onClick={() => handlePaymentMethodTab('CARD')}
              sx={{ fontWeight: 700 }}
            >
              Card
            </Button>
            <Button
              variant={paymentMethod === 'UDHAAR' ? 'contained' : 'outlined'}
              startIcon={<BookOpen size={18} />}
              onClick={() => handlePaymentMethodTab('UDHAAR')}
              sx={{ fontWeight: 700 }}
            >
              Udhaar
            </Button>
            <Button
              variant={paymentMethod === 'MIXED' ? 'contained' : 'outlined'}
              onClick={() => handlePaymentMethodTab('MIXED')}
              sx={{ fontWeight: 700 }}
            >
              Split
            </Button>
          </Stack>

          {/* Method Details */}
          {paymentMethod === 'CASH' && (
            <Box sx={{ textAlign: 'center', py: 2 }}>
              <TextField
                label="Cash Tendered (₹)"
                type="number"
                value={tenderedCash}
                onChange={(e) => setTenderedCash(Number(e.target.value) || 0)}
                fullWidth
                sx={{ mb: 2 }}
                InputProps={{
                  startAdornment: <InputAdornment position="start">₹</InputAdornment>,
                }}
              />
              <Box
                sx={{
                  p: 2,
                  bgcolor: changeDue >= 0 ? 'success.light' : 'error.light',
                  color: changeDue >= 0 ? 'success.contrastText' : 'error.contrastText',
                  borderRadius: 2,
                }}
              >
                <Typography variant="body2">Change Due to Customer:</Typography>
                <Typography variant="h4" fontWeight={800}>
                  ₹{changeDue.toFixed(2)}
                </Typography>
              </Box>
            </Box>
          )}

          {paymentMethod === 'UPI' && (
            <Box sx={{ textAlign: 'center', py: 2 }}>
              <Box
                sx={{
                  display: 'inline-block',
                  p: 2,
                  bgcolor: 'white',
                  border: '2px dashed #10b981',
                  borderRadius: 3,
                  mb: 2,
                }}
              >
                {/* Simulated dynamic QR code */}
                <Box
                  sx={{
                    width: 180,
                    height: 180,
                    bgcolor: '#0f172a',
                    color: '#fff',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: 2,
                  }}
                >
                  <QrCode size={100} color="#10b981" />
                  <Typography variant="caption" sx={{ mt: 1, fontWeight: 700 }}>
                    BHIM / PhonePe / GPay
                  </Typography>
                  <Typography variant="body2" fontWeight={800}>
                    ₹{summary.grandTotal}
                  </Typography>
                </Box>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Scan using any UPI app to pay ₹{summary.grandTotal} instantly.
              </Typography>
            </Box>
          )}

          {paymentMethod === 'CARD' && (
            <Box sx={{ textAlign: 'center', py: 3 }}>
              <CreditCard size={48} color="#0284c7" />
              <Typography variant="h6" fontWeight={700} sx={{ mt: 1 }}>
                Swipe or Tap Card on POS Terminal
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                Amount to charge: ₹{summary.grandTotal}
              </Typography>
            </Box>
          )}

          {paymentMethod === 'UDHAAR' && (
            <Box sx={{ py: 2 }}>
              {selectedCustomer ? (
                <Card variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
                  <Typography variant="body1" fontWeight={700}>
                    Khata Credit Sale for {selectedCustomer.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Phone: {selectedCustomer.phone}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Current Outstanding Balance: ₹{selectedCustomer.outstandingBalance}
                  </Typography>
                  <Typography variant="body2" color="primary.main" fontWeight={700} sx={{ mt: 1 }}>
                    New Balance after this bill: ₹
                    {(Number(selectedCustomer.outstandingBalance) + summary.grandTotal).toFixed(2)}
                  </Typography>
                </Card>
              ) : (
                <Alert severity="warning">
                  Please close this dialog and select a registered customer first to use Udhaar.
                </Alert>
              )}
            </Box>
          )}

          {paymentMethod === 'MIXED' && (
            <Grid container spacing={2} sx={{ py: 1 }}>
              <Grid item xs={6}>
                <TextField
                  label="Cash (₹)"
                  type="number"
                  fullWidth
                  value={cashAmount}
                  onChange={(e) => setCashAmount(Number(e.target.value) || 0)}
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="UPI (₹)"
                  type="number"
                  fullWidth
                  value={upiAmount}
                  onChange={(e) => setUpiAmount(Number(e.target.value) || 0)}
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Card (₹)"
                  type="number"
                  fullWidth
                  value={cardAmount}
                  onChange={(e) => setCardAmount(Number(e.target.value) || 0)}
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Udhaar (₹)"
                  type="number"
                  fullWidth
                  value={udhaarAmount}
                  onChange={(e) => setUdhaarAmount(Number(e.target.value) || 0)}
                />
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setPaymentModalOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleCompleteSale}
            disabled={submitting}
            sx={{ px: 4, fontWeight: 700 }}
          >
            {submitting ? 'Processing...' : 'Complete Sale & Print Bill'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Quick Add Customer Dialog */}
      <Dialog open={customerModalOpen} onClose={() => setCustomerModalOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle fontWeight={700}>Quick Add Customer</DialogTitle>
        <DialogContent dividers>
          <TextField
            autoFocus
            margin="dense"
            label="Customer Name"
            fullWidth
            required
            value={newCustName}
            onChange={(e) => setNewCustName(e.target.value)}
          />
          <TextField
            margin="dense"
            label="Phone Number"
            fullWidth
            required
            value={newCustPhone}
            onChange={(e) => setNewCustPhone(e.target.value)}
          />
          <TextField
            margin="dense"
            label="Credit Limit (₹)"
            type="number"
            fullWidth
            value={newCustCreditLimit}
            onChange={(e) => setNewCustCreditLimit(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCustomerModalOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateCustomer}>
            Save Customer
          </Button>
        </DialogActions>
      </Dialog>

      {/* Invoice Print Modal */}
      {completedSale && (
        <PrintInvoiceModal
          open={printModalOpen}
          onClose={() => setPrintModalOpen(false)}
          sale={completedSale}
        />
      )}
    </Box>
  );
};
