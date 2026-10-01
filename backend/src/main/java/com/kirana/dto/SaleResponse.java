package com.kirana.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class SaleResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private String storeAddress;
    private String storeCity;
    private String storeState;
    private String storePincode;
    private String storePhone;
    private String storeGstin;
    private String invoiceFooterMessage;
    private String invoiceTerms;
    private int thermalPaperWidthMm;

    private Long customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal customerCurrentOutstanding;

    private String cashierName;
    private String invoiceNumber;
    private Instant saleDate;

    private BigDecimal subtotal;
    private BigDecimal itemDiscountTotal;
    private BigDecimal billDiscountRate;
    private BigDecimal billDiscountAmount;
    private BigDecimal taxableAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal roundOff;
    private BigDecimal totalAmount;
    private BigDecimal totalCogs;
    private BigDecimal grossProfit;
    private BigDecimal paidAmount;

    private String status;
    private String paymentStatus;
    private String notes;
    private String cancelReason;

    private List<SaleItemResponseDto> items;
    private List<SalePaymentResponseDto> payments;

    public static class SaleItemResponseDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productBarcode;
        private String productSku;
        private String unitName;
        private Long batchId;
        private String batchNumber;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal costPrice;
        private BigDecimal discountAmount;
        private BigDecimal taxableAmount;
        private BigDecimal gstRate;
        private BigDecimal cgstAmount;
        private BigDecimal sgstAmount;
        private BigDecimal igstAmount;
        private BigDecimal totalTax;
        private BigDecimal lineTotal;
        private BigDecimal lineProfit;
        private BigDecimal returnedQuantity;

        public SaleItemResponseDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getProductBarcode() { return productBarcode; }
        public void setProductBarcode(String productBarcode) { this.productBarcode = productBarcode; }

        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }

        public String getUnitName() { return unitName; }
        public void setUnitName(String unitName) { this.unitName = unitName; }

        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }

        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getCostPrice() { return costPrice; }
        public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

        public BigDecimal getTaxableAmount() { return taxableAmount; }
        public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

        public BigDecimal getGstRate() { return gstRate; }
        public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }

        public BigDecimal getCgstAmount() { return cgstAmount; }
        public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

        public BigDecimal getSgstAmount() { return sgstAmount; }
        public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

        public BigDecimal getIgstAmount() { return igstAmount; }
        public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

        public BigDecimal getTotalTax() { return totalTax; }
        public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }

        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

        public BigDecimal getLineProfit() { return lineProfit; }
        public void setLineProfit(BigDecimal lineProfit) { this.lineProfit = lineProfit; }

        public BigDecimal getReturnedQuantity() { return returnedQuantity; }
        public void setReturnedQuantity(BigDecimal returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    }

    public static class SalePaymentResponseDto {
        private Long id;
        private String paymentMethod;
        private BigDecimal amount;
        private String transactionRef;
        private String notes;

        public SalePaymentResponseDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getTransactionRef() { return transactionRef; }
        public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public SaleResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }

    public String getStoreCity() { return storeCity; }
    public void setStoreCity(String storeCity) { this.storeCity = storeCity; }

    public String getStoreState() { return storeState; }
    public void setStoreState(String storeState) { this.storeState = storeState; }

    public String getStorePincode() { return storePincode; }
    public void setStorePincode(String storePincode) { this.storePincode = storePincode; }

    public String getStorePhone() { return storePhone; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }

    public String getStoreGstin() { return storeGstin; }
    public void setStoreGstin(String storeGstin) { this.storeGstin = storeGstin; }

    public String getInvoiceFooterMessage() { return invoiceFooterMessage; }
    public void setInvoiceFooterMessage(String invoiceFooterMessage) { this.invoiceFooterMessage = invoiceFooterMessage; }

    public String getInvoiceTerms() { return invoiceTerms; }
    public void setInvoiceTerms(String invoiceTerms) { this.invoiceTerms = invoiceTerms; }

    public int getThermalPaperWidthMm() { return thermalPaperWidthMm; }
    public void setThermalPaperWidthMm(int thermalPaperWidthMm) { this.thermalPaperWidthMm = thermalPaperWidthMm; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public BigDecimal getCustomerCurrentOutstanding() { return customerCurrentOutstanding; }
    public void setCustomerCurrentOutstanding(BigDecimal customerCurrentOutstanding) { this.customerCurrentOutstanding = customerCurrentOutstanding; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Instant getSaleDate() { return saleDate; }
    public void setSaleDate(Instant saleDate) { this.saleDate = saleDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getItemDiscountTotal() { return itemDiscountTotal; }
    public void setItemDiscountTotal(BigDecimal itemDiscountTotal) { this.itemDiscountTotal = itemDiscountTotal; }

    public BigDecimal getBillDiscountRate() { return billDiscountRate; }
    public void setBillDiscountRate(BigDecimal billDiscountRate) { this.billDiscountRate = billDiscountRate; }

    public BigDecimal getBillDiscountAmount() { return billDiscountAmount; }
    public void setBillDiscountAmount(BigDecimal billDiscountAmount) { this.billDiscountAmount = billDiscountAmount; }

    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

    public BigDecimal getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }

    public BigDecimal getRoundOff() { return roundOff; }
    public void setRoundOff(BigDecimal roundOff) { this.roundOff = roundOff; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalCogs() { return totalCogs; }
    public void setTotalCogs(BigDecimal totalCogs) { this.totalCogs = totalCogs; }

    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = grossProfit; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public List<SaleItemResponseDto> getItems() { return items; }
    public void setItems(List<SaleItemResponseDto> items) { this.items = items; }

    public List<SalePaymentResponseDto> getPayments() { return payments; }
    public void setPayments(List<SalePaymentResponseDto> payments) { this.payments = payments; }
}
