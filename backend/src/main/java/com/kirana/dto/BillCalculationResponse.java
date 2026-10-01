package com.kirana.dto;

import java.math.BigDecimal;
import java.util.List;

public class BillCalculationResponse {

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
    private BigDecimal grandTotal;
    private BigDecimal estimatedProfit;
    private List<CalculatedItemDto> items;

    public static class CalculatedItemDto {
        private Long productId;
        private String productName;
        private String productBarcode;
        private Long batchId;
        private String batchNumber;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal costPrice;
        private BigDecimal itemDiscount;
        private BigDecimal taxableAmount;
        private BigDecimal gstRate;
        private BigDecimal cgstAmount;
        private BigDecimal sgstAmount;
        private BigDecimal igstAmount;
        private BigDecimal totalTax;
        private BigDecimal lineTotal;
        private BigDecimal lineProfit;

        public CalculatedItemDto() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getProductBarcode() { return productBarcode; }
        public void setProductBarcode(String productBarcode) { this.productBarcode = productBarcode; }

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

        public BigDecimal getItemDiscount() { return itemDiscount; }
        public void setItemDiscount(BigDecimal itemDiscount) { this.itemDiscount = itemDiscount; }

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
    }

    public BillCalculationResponse() {}

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

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public BigDecimal getEstimatedProfit() { return estimatedProfit; }
    public void setEstimatedProfit(BigDecimal estimatedProfit) { this.estimatedProfit = estimatedProfit; }

    public List<CalculatedItemDto> getItems() { return items; }
    public void setItems(List<CalculatedItemDto> items) { this.items = items; }
}
