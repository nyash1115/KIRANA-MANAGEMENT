package com.kirana.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long unitId;
    private String unitName;
    private String unitCode;
    private boolean allowDecimals;
    private String name;
    private String barcode;
    private String sku;
    private String brand;
    private String hsnCode;
    private BigDecimal gstRate;
    private BigDecimal defaultCostPrice;
    private BigDecimal defaultSellingPrice;
    private BigDecimal defaultMrp;
    private BigDecimal minStockLevel;
    private BigDecimal reorderLevel;
    private BigDecimal totalAvailableStock;
    private String imageUrl;
    private String description;
    private boolean active;
    private Instant createdAt;

    public ProductDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }

    public boolean isAllowDecimals() { return allowDecimals; }
    public void setAllowDecimals(boolean allowDecimals) { this.allowDecimals = allowDecimals; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }

    public BigDecimal getDefaultCostPrice() { return defaultCostPrice; }
    public void setDefaultCostPrice(BigDecimal defaultCostPrice) { this.defaultCostPrice = defaultCostPrice; }

    public BigDecimal getDefaultSellingPrice() { return defaultSellingPrice; }
    public void setDefaultSellingPrice(BigDecimal defaultSellingPrice) { this.defaultSellingPrice = defaultSellingPrice; }

    public BigDecimal getDefaultMrp() { return defaultMrp; }
    public void setDefaultMrp(BigDecimal defaultMrp) { this.defaultMrp = defaultMrp; }

    public BigDecimal getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(BigDecimal minStockLevel) { this.minStockLevel = minStockLevel; }

    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }

    public BigDecimal getTotalAvailableStock() { return totalAvailableStock; }
    public void setTotalAvailableStock(BigDecimal totalAvailableStock) { this.totalAvailableStock = totalAvailableStock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
