package com.kirana.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class InventoryBatchDto {
    private Long id;
    private Long storeId;
    private Long productId;
    private String productName;
    private String productBarcode;
    private String productSku;
    private String unitName;
    private String unitCode;
    private String batchNumber;
    private BigDecimal quantity;
    private BigDecimal initialQuantity;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private LocalDate expiryDate;
    private String status;
    private boolean isExpired;
    private boolean isNearExpiry;
    private Instant createdAt;

    public InventoryBatchDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

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

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(BigDecimal initialQuantity) { this.initialQuantity = initialQuantity; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }

    public boolean isNearExpiry() { return isNearExpiry; }
    public void setNearExpiry(boolean nearExpiry) { isNearExpiry = nearExpiry; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
