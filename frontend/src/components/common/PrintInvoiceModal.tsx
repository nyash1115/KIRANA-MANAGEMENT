import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Tabs,
  Tab,
  Divider,
} from '@mui/material';
import { Printer, X, Download, FileText } from 'lucide-react';
import { Sale } from '../../types';

interface PrintInvoiceModalProps {
  open: boolean;
  sale: Sale | null;
  onClose: () => void;
}

export const PrintInvoiceModal: React.FC<PrintInvoiceModalProps> = ({ open, sale, onClose }) => {
  const [layout, setLayout] = useState<'thermal' | 'a4'>('thermal');

  if (!sale) return null;

  const handlePrint = () => {
    const isThermal = layout === 'thermal';
    const elementId = isThermal ? 'printable-receipt' : 'printable-a4';
    const elem = document.getElementById(elementId);
    if (!elem) {
      window.print();
      return;
    }

    try {
      let iframe = document.getElementById('pos-print-iframe') as HTMLIFrameElement;
      if (!iframe) {
        iframe = document.createElement('iframe');
        iframe.id = 'pos-print-iframe';
        iframe.style.position = 'fixed';
        iframe.style.right = '0';
        iframe.style.bottom = '0';
        iframe.style.width = '0';
        iframe.style.height = '0';
        iframe.style.border = '0';
        iframe.style.visibility = 'hidden';
        document.body.appendChild(iframe);
      }

      const doc = iframe.contentWindow?.document;
      if (doc) {
        doc.open();
        doc.write(`
          <!DOCTYPE html>
          <html>
            <head>
              <meta charset="utf-8" />
              <title>${isThermal ? 'Receipt' : 'Invoice'}-${sale.invoiceNumber}</title>
              <style>
                @page {
                  size: ${isThermal ? '80mm auto' : 'A4 portrait'};
                  margin: ${isThermal ? '0mm' : '8mm'};
                }
                * {
                  box-sizing: border-box;
                  margin: 0;
                  padding: 0;
                  -webkit-print-color-adjust: exact !important;
                  print-color-adjust: exact !important;
                }
                body {
                  background: #fff;
                  color: #000;
                  font-family: ${isThermal ? '"Courier New", Courier, monospace' : '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif'};
                  font-size: ${isThermal ? '11px' : '12px'};
                  padding: ${isThermal ? '3mm 2.5mm' : '0'};
                  width: ${isThermal ? '72mm' : '100%'};
                  margin: 0 auto;
                }
                table {
                  width: 100%;
                  border-collapse: collapse;
                }
                .no-print {
                  display: none !important;
                }
              </style>
            </head>
            <body>
              ${elem.innerHTML}
            </body>
          </html>
        `);
        doc.close();

        setTimeout(() => {
          if (iframe.contentWindow) {
            iframe.contentWindow.focus();
            iframe.contentWindow.print();
          }
        }, 250);
        return;
      }
    } catch (e) {
      console.warn('Iframe print error, falling back to window.print', e);
    }

    window.print();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <FileText size={22} color="#10B981" />
          <Typography variant="h6" fontWeight={700}>
            Tax Invoice #{sale.invoiceNumber}
          </Typography>
        </Box>
        <Button onClick={onClose} size="small" sx={{ minWidth: 32 }}>
          <X size={20} />
        </Button>
      </DialogTitle>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
        <Tabs value={layout} onChange={(_, v) => setLayout(v)}>
          <Tab label="Thermal Receipt (80mm)" value="thermal" />
          <Tab label="Standard A4 Invoice" value="a4" />
        </Tabs>
      </Box>

      <DialogContent sx={{ p: 3, display: 'flex', justifyContent: 'center', backgroundColor: '#f1f5f9' }}>
        {layout === 'thermal' ? (
          // THERMAL RECEIPT 80mm
          <Box
            id="printable-receipt"
            sx={{
              width: '80mm',
              backgroundColor: '#fff',
              color: '#000',
              p: 2,
              fontFamily: '"Courier New", Courier, monospace',
              fontSize: '12px',
              boxShadow: '0 4px 14px rgba(0,0,0,0.1)',
              borderRadius: 1,
            }}
          >
            <Box textAlign="center" mb={1}>
              <Typography sx={{ fontWeight: 900, fontSize: '15px' }}>{sale.storeName}</Typography>
              <Typography sx={{ fontSize: '11px' }}>{sale.storeAddress}</Typography>
              <Typography sx={{ fontSize: '11px' }}>Ph: {sale.storePhone}</Typography>
              {sale.storeGstin && <Typography sx={{ fontSize: '11px', fontWeight: 'bold' }}>GSTIN: {sale.storeGstin}</Typography>}
            </Box>

            <Box sx={{ borderTop: '1px dashed #000', my: 1 }} />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px' }}>
              <span>Bill No: {sale.invoiceNumber}</span>
              <span>{new Date(sale.saleDate).toLocaleDateString()}</span>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px' }}>
              <span>Cashier: {sale.cashierName}</span>
              <span>{new Date(sale.saleDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
            </Box>
            {sale.customerName && (
              <Box sx={{ fontSize: '11px', mt: 0.5 }}>
                <span>Cust: {sale.customerName} ({sale.customerPhone})</span>
              </Box>
            )}

            <Box sx={{ borderTop: '1px dashed #000', my: 1 }} />

            {/* Table Header */}
            <Box sx={{ display: 'flex', fontWeight: 'bold', fontSize: '11px' }}>
              <span style={{ flex: 2 }}>Item</span>
              <span style={{ width: '40px', textAlign: 'right' }}>Qty</span>
              <span style={{ width: '50px', textAlign: 'right' }}>Rate</span>
              <span style={{ width: '55px', textAlign: 'right' }}>Total</span>
            </Box>
            <Box sx={{ borderTop: '1px dashed #000', my: 0.5 }} />

            {/* Items */}
            {sale.items.map((item, idx) => (
              <Box key={idx} sx={{ my: 0.5, fontSize: '11px' }}>
                <Box sx={{ fontWeight: 'bold' }}>{item.productName}</Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ flex: 2, color: '#555' }}>
                    GST {item.gstRate}% {item.discountAmount > 0 ? `(Disc -₹${item.discountAmount})` : ''}
                  </span>
                  <span style={{ width: '40px', textAlign: 'right' }}>{item.quantity}</span>
                  <span style={{ width: '50px', textAlign: 'right' }}>₹{item.unitPrice}</span>
                  <span style={{ width: '55px', textAlign: 'right', fontWeight: 'bold' }}>₹{item.lineTotal.toFixed(2)}</span>
                </Box>
              </Box>
            ))}

            <Box sx={{ borderTop: '1px dashed #000', my: 1 }} />

            {/* Financial Summary */}
            <Box sx={{ fontSize: '11px', lineHeight: 1.5 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Subtotal:</span>
                <span>₹{sale.subtotal.toFixed(2)}</span>
              </Box>
              {(sale.itemDiscountTotal > 0 || sale.billDiscountAmount > 0) && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between', color: '#16a34a' }}>
                  <span>Discounts:</span>
                  <span>-₹{(sale.itemDiscountTotal + sale.billDiscountAmount).toFixed(2)}</span>
                </Box>
              )}
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>Taxable Value:</span>
                <span>₹{sale.taxableAmount.toFixed(2)}</span>
              </Box>
              {sale.cgstAmount > 0 && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>CGST:</span>
                  <span>₹{sale.cgstAmount.toFixed(2)}</span>
                </Box>
              )}
              {sale.sgstAmount > 0 && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>SGST:</span>
                  <span>₹{sale.sgstAmount.toFixed(2)}</span>
                </Box>
              )}
              {sale.igstAmount > 0 && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>IGST:</span>
                  <span>₹{sale.igstAmount.toFixed(2)}</span>
                </Box>
              )}
              {sale.roundOff !== 0 && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Round Off:</span>
                  <span>{sale.roundOff > 0 ? `+₹${sale.roundOff.toFixed(2)}` : `-₹${Math.abs(sale.roundOff).toFixed(2)}`}</span>
                </Box>
              )}
              <Box sx={{ borderTop: '1px dashed #000', my: 0.5 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', fontWeight: 900, fontSize: '14px' }}>
                <span>GRAND TOTAL:</span>
                <span>₹{sale.totalAmount.toFixed(2)}</span>
              </Box>
            </Box>

            <Box sx={{ borderTop: '1px dashed #000', my: 1 }} />

            {/* Payment Modes */}
            <Box sx={{ fontSize: '11px' }}>
              <Typography sx={{ fontWeight: 'bold', fontSize: '11px' }}>Payment Mode(s):</Typography>
              {sale.payments.map((p, i) => (
                <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>{p.paymentMethod}:</span>
                  <span>₹{p.amount.toFixed(2)}</span>
                </Box>
              ))}
              {sale.customerCurrentOutstanding !== undefined && sale.customerCurrentOutstanding > 0 && (
                <Box sx={{ mt: 0.5, p: 0.5, backgroundColor: '#fef3c7', borderRadius: 0.5 }}>
                  <span style={{ fontWeight: 'bold' }}>Current Khata Balance: </span>
                  <span>₹{sale.customerCurrentOutstanding.toFixed(2)}</span>
                </Box>
              )}
            </Box>

            <Box sx={{ borderTop: '1px dashed #000', my: 1 }} />

            <Box textAlign="center" sx={{ fontSize: '10px' }}>
              <Typography sx={{ fontSize: '10px', fontStyle: 'italic' }}>{sale.invoiceFooterMessage}</Typography>
              <Typography sx={{ fontSize: '9px', color: '#666', mt: 0.5 }}>{sale.invoiceTerms}</Typography>
            </Box>
          </Box>
        ) : (
          // A4 STANDARD TAX INVOICE
          <Box
            id="printable-a4"
            sx={{
              width: '100%',
              maxWidth: '700px',
              backgroundColor: '#fff',
              color: '#000',
              p: 4,
              boxShadow: '0 4px 14px rgba(0,0,0,0.1)',
              borderRadius: 2,
            }}
          >
            {/* Header */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', borderBottom: '2px solid #10B981', pb: 2 }}>
              <Box>
                <Typography variant="h5" fontWeight={800} color="#10B981">
                  {sale.storeName}
                </Typography>
                <Typography variant="body2">{sale.storeAddress}, {sale.storeCity}, {sale.storeState} - {sale.storePincode}</Typography>
                <Typography variant="body2">Phone: {sale.storePhone}</Typography>
                {sale.storeGstin && <Typography variant="body2" fontWeight="bold">GSTIN: {sale.storeGstin}</Typography>}
              </Box>
              <Box textAlign="right">
                <Typography variant="h6" fontWeight={700}>TAX INVOICE</Typography>
                <Typography variant="body2"><strong>Invoice No:</strong> {sale.invoiceNumber}</Typography>
                <Typography variant="body2"><strong>Date:</strong> {new Date(sale.saleDate).toLocaleDateString()}</Typography>
                <Typography variant="body2"><strong>Cashier:</strong> {sale.cashierName}</Typography>
              </Box>
            </Box>

            {/* Billed to */}
            <Box sx={{ my: 2, p: 1.5, backgroundColor: '#f8fafc', borderRadius: 1 }}>
              <Typography variant="subtitle2" fontWeight={700}>Billed To:</Typography>
              <Typography variant="body2"><strong>Name:</strong> {sale.customerName || 'Walk-in Customer'}</Typography>
              {sale.customerPhone && <Typography variant="body2"><strong>Phone:</strong> {sale.customerPhone}</Typography>}
            </Box>

            {/* Line items table */}
            <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '16px', fontSize: '13px' }}>
              <thead>
                <tr style={{ backgroundColor: '#10B981', color: '#fff', textAlign: 'left' }}>
                  <th style={{ padding: '8px' }}>#</th>
                  <th style={{ padding: '8px' }}>Item Description</th>
                  <th style={{ padding: '8px', textAlign: 'right' }}>Qty</th>
                  <th style={{ padding: '8px', textAlign: 'right' }}>Rate</th>
                  <th style={{ padding: '8px', textAlign: 'right' }}>GST %</th>
                  <th style={{ padding: '8px', textAlign: 'right' }}>Tax</th>
                  <th style={{ padding: '8px', textAlign: 'right' }}>Total</th>
                </tr>
              </thead>
              <tbody>
                {sale.items.map((item, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid #e2e8f0' }}>
                    <td style={{ padding: '8px' }}>{idx + 1}</td>
                    <td style={{ padding: '8px', fontWeight: 600 }}>{item.productName}</td>
                    <td style={{ padding: '8px', textAlign: 'right' }}>{item.quantity} {item.unitName}</td>
                    <td style={{ padding: '8px', textAlign: 'right' }}>₹{item.unitPrice}</td>
                    <td style={{ padding: '8px', textAlign: 'right' }}>{item.gstRate}%</td>
                    <td style={{ padding: '8px', textAlign: 'right' }}>₹{item.totalTax.toFixed(2)}</td>
                    <td style={{ padding: '8px', textAlign: 'right', fontWeight: 700 }}>₹{item.lineTotal.toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Summary */}
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 3 }}>
              <Box sx={{ width: '250px', fontSize: '13px', lineHeight: 1.8 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Subtotal:</span>
                  <strong>₹{sale.subtotal.toFixed(2)}</strong>
                </Box>
                {sale.itemDiscountTotal + sale.billDiscountAmount > 0 && (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', color: '#16a34a' }}>
                    <span>Discounts:</span>
                    <strong>-₹{(sale.itemDiscountTotal + sale.billDiscountAmount).toFixed(2)}</strong>
                  </Box>
                )}
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Taxable Value:</span>
                  <strong>₹{sale.taxableAmount.toFixed(2)}</strong>
                </Box>
                {sale.cgstAmount > 0 && (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>CGST:</span>
                    <strong>₹{sale.cgstAmount.toFixed(2)}</strong>
                  </Box>
                )}
                {sale.sgstAmount > 0 && (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>SGST:</span>
                    <strong>₹{sale.sgstAmount.toFixed(2)}</strong>
                  </Box>
                )}
                <Divider sx={{ my: 1 }} />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', fontSize: '16px', fontWeight: 800, color: '#10B981' }}>
                  <span>Grand Total:</span>
                  <span>₹{sale.totalAmount.toFixed(2)}</span>
                </Box>
              </Box>
            </Box>

            <Divider sx={{ my: 3 }} />
            <Box sx={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: '#64748b' }}>
              <Box>
                <Typography variant="caption" display="block">{sale.invoiceFooterMessage}</Typography>
                <Typography variant="caption" display="block">{sale.invoiceTerms}</Typography>
              </Box>
              <Box textAlign="right">
                <Typography variant="caption" display="block">For {sale.storeName}</Typography>
                <Typography variant="caption" sx={{ mt: 3, display: 'block' }}>Authorized Signatory</Typography>
              </Box>
            </Box>
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ p: 2, px: 3 }}>
        <Button onClick={onClose} variant="outlined" color="inherit">
          Close
        </Button>
        <Button onClick={handlePrint} variant="contained" color="primary" startIcon={<Printer size={18} />}>
          Print Receipt
        </Button>
      </DialogActions>

      {/* Scoped CSS to ensure browser direct print (Ctrl+P) never prints dialog chrome or buttons */}
      <style>{`
        @media print {
          body * {
            visibility: hidden !important;
          }
          .MuiDialog-container,
          .MuiBackdrop-root,
          .MuiDialogTitle-root,
          .MuiDialogActions-root,
          .MuiTabs-root,
          button {
            display: none !important;
          }
          #printable-receipt,
          #printable-receipt * {
            visibility: visible !important;
          }
          #printable-receipt {
            position: fixed !important;
            left: 0 !important;
            top: 0 !important;
            width: 76mm !important;
            margin: 0 !important;
            padding: 2mm !important;
            box-shadow: none !important;
            border: none !important;
            background: #fff !important;
            color: #000 !important;
            z-index: 999999 !important;
          }
          #printable-a4,
          #printable-a4 * {
            visibility: visible !important;
          }
          #printable-a4 {
            position: fixed !important;
            left: 0 !important;
            top: 0 !important;
            width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
            box-shadow: none !important;
            border: none !important;
            background: #fff !important;
            color: #000 !important;
            z-index: 999999 !important;
          }
        }
      `}</style>
    </Dialog>
  );
};
