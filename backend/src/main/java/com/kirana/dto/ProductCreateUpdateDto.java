package com.kirana.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductCreateUpdateDto {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Unit is required")
    private Long unitId;

    private String barcode;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String brand;
    private String hsnCode;

    @NotNull(message = "GST rate is required")
    @DecimalMin(value = "0.0", message = "GST rate cannot be negative")
    private BigDecimal gstRate;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    private BigDecimal defaultCostPrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    private BigDecimal defaultSellingPrice;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", message = "MRP cannot be negative")
    private BigDecimal defaultMrp;

    private BigDecimal minStockLevel = new BigDecimal("5.000");
    private BigDecimal reorderLevel = new BigDecimal("10.000");
    private String imageUrl;
    private String description;

    public ProductCreateUpdateDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
