import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Stack,
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
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  MenuItem,
  CircularProgress,
  TablePagination,
  Tooltip,
  Alert,
} from '@mui/material';
import { Search, Plus, Edit2, Barcode, AlertTriangle } from 'lucide-react';
import { productService, categoryService, unitService } from '../services/api';
import { Product, Category, Unit, ProductRequest } from '../types';

export const ProductsPage: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [units, setUnits] = useState<Unit[]>([]);
  const [loading, setLoading] = useState(true);

  // Search & Pagination
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  // Dialog State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  // Form State
  const [formData, setFormData] = useState<ProductRequest>({
    categoryId: 1,
    unitId: 1,
    name: '',
    barcode: '',
    sku: '',
    brand: '',
    hsnCode: '',
    gstRate: 5,
    defaultCostPrice: 0,
    defaultSellingPrice: 0,
    defaultMrp: 0,
    minStockLevel: 5,
    reorderLevel: 10,
    active: true,
  });

  useEffect(() => {
    loadLookups();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [page, rowsPerPage, search, selectedCategory]);

  const loadLookups = async () => {
    try {
      const [catRes, unitRes] = await Promise.all([
        categoryService.getAll(),
        unitService.getAll(),
      ]);
      setCategories(catRes.data);
      setUnits(unitRes.data);
    } catch (e) {
      console.error('Failed to load lookups', e);
    }
  };

  const loadProducts = async () => {
    try {
      setLoading(true);
      const res = await productService.getAll({
        page,
        size: rowsPerPage,
        search: search.trim() || undefined,
        categoryId: selectedCategory ? Number(selectedCategory) : undefined,
      });
      setProducts(res.data.content);
      setTotalElements(res.data.totalElements);
    } catch (e) {
      console.error('Failed to load products', e);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingProduct(null);
    setFormData({
      categoryId: categories[0]?.id || 1,
      unitId: units[0]?.id || 1,
      name: '',
      barcode: '',
      sku: 'SKU-' + Math.floor(1000 + Math.random() * 9000),
      brand: '',
      hsnCode: '1904',
      gstRate: 5,
      defaultCostPrice: 0,
      defaultSellingPrice: 0,
      defaultMrp: 0,
      minStockLevel: 5,
      reorderLevel: 10,
      active: true,
    });
    setFormError(null);
    setModalOpen(true);
  };

  const handleOpenEdit = (prod: Product) => {
    setEditingProduct(prod);
    setFormData({
      categoryId: prod.categoryId,
      unitId: prod.unitId,
      name: prod.name,
      barcode: prod.barcode || '',
      sku: prod.sku,
      brand: prod.brand || '',
      hsnCode: prod.hsnCode || '',
      gstRate: prod.gstRate,
      defaultCostPrice: prod.defaultCostPrice,
      defaultSellingPrice: prod.defaultSellingPrice,
      defaultMrp: prod.defaultMrp,
      minStockLevel: prod.minStockLevel,
      reorderLevel: prod.reorderLevel,
      active: prod.active,
    });
    setFormError(null);
    setModalOpen(true);
  };

  const handleSaveProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      setFormError('Product Name is required.');
      return;
    }
    if (formData.defaultSellingPrice < formData.defaultCostPrice) {
      setFormError('Selling price is less than cost price. Please verify pricing.');
    }

    try {
      setSaving(true);
      setFormError(null);
      if (editingProduct) {
        await productService.update(editingProduct.id, formData);
      } else {
        await productService.create(formData);
      }
      setModalOpen(false);
      loadProducts();
    } catch (err: any) {
      setFormError(err?.response?.data?.message || 'Failed to save product.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Top Header */}
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight={800} letterSpacing="-0.5px">
            Product Master Catalog
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage kirana item details, barcodes, GST tax slabs, HSN, and default prices
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Plus size={18} />}
          onClick={handleOpenAdd}
          sx={{ fontWeight: 700, px: 2.5 }}
        >
          Add Product
        </Button>
      </Stack>

      {/* Filter / Search Bar */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={5}>
            <TextField
              size="small"
              fullWidth
              placeholder="Search by product name, barcode, or SKU..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search size={18} color="#64748b" />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              select
              size="small"
              fullWidth
              label="Filter by Category"
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setPage(0);
              }}
            >
              <MenuItem value="">All Categories</MenuItem>
              {categories.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  {c.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} md={3} sx={{ textAlign: { md: 'right' } }}>
            <Typography variant="body2" color="text.secondary">
              Total Products: <strong>{totalElements}</strong>
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* Products Table */}
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>Product Name</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Barcode / SKU</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Category</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>MRP</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Selling Price</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Cost Price</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>GST</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Stock</TableCell>
                <TableCell align="center" sx={{ fontWeight: 700 }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={32} />
                  </TableCell>
                </TableRow>
              ) : products.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No products found matching your search.
                  </TableCell>
                </TableRow>
              ) : (
                products.map((p) => {
                  const stock = p.totalAvailableStock || 0;
                  const isLow = stock > 0 && stock <= p.reorderLevel;
                  const isOut = stock <= 0;

                  return (
                    <TableRow key={p.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight={700}>
                          {p.name}
                        </Typography>
                        {p.brand && (
                          <Typography variant="caption" color="text.secondary">
                            Brand: {p.brand}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <Stack direction="row" spacing={0.5} alignItems="center">
                          <Barcode size={16} color="#64748b" />
                          <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                            {p.barcode || 'N/A'}
                          </Typography>
                        </Stack>
                        <Typography variant="caption" color="text.secondary" display="block">
                          SKU: {p.sku}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip size="small" label={p.categoryName} sx={{ fontWeight: 600 }} />
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" sx={{ textDecoration: 'line-through', color: 'text.secondary' }}>
                          ₹{p.defaultMrp}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" fontWeight={800} color="primary.main">
                          ₹{p.defaultSellingPrice}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Typography variant="body2" color="text.secondary">
                          ₹{p.defaultCostPrice}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          size="small"
                          label={`${p.gstRate}%`}
                          color={p.gstRate > 0 ? 'info' : 'default'}
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Chip
                          size="small"
                          label={`${stock} ${p.unitCode}`}
                          color={isOut ? 'error' : isLow ? 'warning' : 'success'}
                          sx={{ fontWeight: 700 }}
                        />
                      </TableCell>
                      <TableCell align="center">
                        <Tooltip title="Edit Product">
                          <IconButton size="small" onClick={() => handleOpenEdit(p)}>
                            <Edit2 size={16} />
                          </IconButton>
                        </Tooltip>
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

      {/* Add / Edit Product Dialog */}
      <Dialog
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          {editingProduct ? `Edit Product: ${editingProduct.name}` : 'Add New Grocery Product'}
        </DialogTitle>
        <DialogContent dividers>
          {formError && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              {formError}
            </Alert>
          )}

          <Grid container spacing={2}>
            <Grid item xs={12} sm={8}>
              <TextField
                required
                fullWidth
                label="Product Name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g. Fortune Sunlite Sunflower Oil 1L"
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Brand"
                value={formData.brand}
                onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                placeholder="e.g. Fortune"
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Barcode (EAN/UPC)"
                value={formData.barcode}
                onChange={(e) => setFormData({ ...formData, barcode: e.target.value })}
                placeholder="e.g. 8901234567890"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                required
                fullWidth
                label="SKU"
                value={formData.sku}
                onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                select
                required
                fullWidth
                label="Category"
                value={formData.categoryId}
                onChange={(e) => setFormData({ ...formData, categoryId: Number(e.target.value) })}
              >
                {categories.map((c) => (
                  <MenuItem key={c.id} value={c.id}>
                    {c.name}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                select
                required
                fullWidth
                label="Unit of Measurement"
                value={formData.unitId}
                onChange={(e) => setFormData({ ...formData, unitId: Number(e.target.value) })}
              >
                {units.map((u) => (
                  <MenuItem key={u.id} value={u.id}>
                    {u.name} ({u.code})
                  </MenuItem>
                ))}
              </TextField>
            </Grid>

            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="HSN Code"
                value={formData.hsnCode}
                onChange={(e) => setFormData({ ...formData, hsnCode: e.target.value })}
                placeholder="e.g. 1512"
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                select
                required
                fullWidth
                label="GST Rate (%)"
                value={formData.gstRate}
                onChange={(e) => setFormData({ ...formData, gstRate: Number(e.target.value) })}
              >
                <MenuItem value={0}>0% (Exempted / Nil)</MenuItem>
                <MenuItem value={5}>5% (Staples / Groceries)</MenuItem>
                <MenuItem value={12}>12% (Processed Food / Dairy)</MenuItem>
                <MenuItem value={18}>18% (Biscuits / Personal Care)</MenuItem>
                <MenuItem value={28}>28% (Luxury / Aerated Drinks)</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                type="number"
                fullWidth
                label="Maximum Retail Price (MRP ₹)"
                value={formData.defaultMrp}
                onChange={(e) => setFormData({ ...formData, defaultMrp: Number(e.target.value) })}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                type="number"
                fullWidth
                label="Cost Price (Purchase Base ₹)"
                value={formData.defaultCostPrice}
                onChange={(e) => setFormData({ ...formData, defaultCostPrice: Number(e.target.value) })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                type="number"
                fullWidth
                label="Selling Price (₹)"
                value={formData.defaultSellingPrice}
                onChange={(e) => setFormData({ ...formData, defaultSellingPrice: Number(e.target.value) })}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                type="number"
                fullWidth
                label="Reorder Alert Level"
                value={formData.reorderLevel}
                onChange={(e) => setFormData({ ...formData, reorderLevel: Number(e.target.value) })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                type="number"
                fullWidth
                label="Minimum Safety Stock"
                value={formData.minStockLevel}
                onChange={(e) => setFormData({ ...formData, minStockLevel: Number(e.target.value) })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setModalOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSaveProduct}
            disabled={saving}
            sx={{ px: 3, fontWeight: 700 }}
          >
            {saving ? <CircularProgress size={20} /> : editingProduct ? 'Update Product' : 'Save Product'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
