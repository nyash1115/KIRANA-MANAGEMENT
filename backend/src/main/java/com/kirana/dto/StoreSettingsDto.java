package com.kirana.dto;

import java.math.BigDecimal;

public class StoreSettingsDto {
    private Long id;
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String storeGstin;
    private boolean taxInclusivePricing;
    private boolean enableIgst;
    private boolean allowNegativeStock;
    private BigDecimal defaultGstRate;
    private BigDecimal lowStockThresholdDefault;
    private int expiryAlertDaysDefault;
    private String invoiceFooterMessage;
    private String invoiceTerms;
    private int thermalPaperWidthMm;
    private String currencySymbol;
    private String currencyCode;

    public StoreSettingsDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }

    public String getStorePhone() { return storePhone; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }

    public String getStoreGstin() { return storeGstin; }
    public void setStoreGstin(String storeGstin) { this.storeGstin = storeGstin; }

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
}
