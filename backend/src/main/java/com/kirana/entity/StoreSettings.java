package com.kirana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "store_settings")
public class StoreSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @Column(name = "tax_inclusive_pricing", nullable = false)
    private boolean taxInclusivePricing = true;

    @Column(name = "enable_igst", nullable = false)
    private boolean enableIgst = false;

    @Column(name = "allow_negative_stock", nullable = false)
    private boolean allowNegativeStock = false;

    @Column(name = "default_gst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultGstRate = BigDecimal.ZERO;

    @Column(name = "low_stock_threshold_default", nullable = false, precision = 12, scale = 3)
    private BigDecimal lowStockThresholdDefault = new BigDecimal("5.000");

    @Column(name = "expiry_alert_days_default", nullable = false)
    private int expiryAlertDaysDefault = 30;

    @Column(name = "invoice_footer_message", columnDefinition = "TEXT")
    private String invoiceFooterMessage = "Thank you for shopping with us! Visit again.";

    @Column(name = "invoice_terms", columnDefinition = "TEXT")
    private String invoiceTerms = "Goods once sold will only be exchanged within 3 days with bill.";

    @Column(name = "thermal_paper_width_mm", nullable = false)
    private int thermalPaperWidthMm = 80;

    @Column(name = "currency_symbol", nullable = false, length = 10)
    private String currencySymbol = "₹";

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode = "INR";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public StoreSettings() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public boolean isTaxInclusivePricing() { return taxInclusivePricing; }
    public void setTaxInclusivePricing(boolean taxInclusivePricing) { this.taxInclusivePricing = taxInclusivePricing; }

    public boolean isEnableIgst() { return enableIgst; }
    public void setEnableIgst(boolean enableIgst) { this.enableIgst = enableIgst; }

    public boolean isAllowNegativeStock() { return allowNegativeStock; }
    public void setAllowNegativeStock(boolean allowNegativeStock) { this.allowNegativeStock = allowNegativeStock; }

    public BigDecimal getDefaultGstRate() { return defaultGstRate; }
    public void setDefaultGstRate(BigDecimal defaultGstRate) { this.defaultGstRate = defaultGstRate; }

    public BigDecimal getLowStockThresholdDefault() { return lowStockThresholdDefault; }
    public void setLowStockThresholdDefault(BigDecimal lowStockThresholdDefault) { this.lowStockThresholdDefault = lowStockThresholdDefault; }

    public int getExpiryAlertDaysDefault() { return expiryAlertDaysDefault; }
    public void setExpiryAlertDaysDefault(int expiryAlertDaysDefault) { this.expiryAlertDaysDefault = expiryAlertDaysDefault; }

    public String getInvoiceFooterMessage() { return invoiceFooterMessage; }
    public void setInvoiceFooterMessage(String invoiceFooterMessage) { this.invoiceFooterMessage = invoiceFooterMessage; }

    public String getInvoiceTerms() { return invoiceTerms; }
    public void setInvoiceTerms(String invoiceTerms) { this.invoiceTerms = invoiceTerms; }

    public int getThermalPaperWidthMm() { return thermalPaperWidthMm; }
    public void setThermalPaperWidthMm(int thermalPaperWidthMm) { this.thermalPaperWidthMm = thermalPaperWidthMm; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
