package com.kirana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, length = 100)
    private String barcode;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(length = 100)
    private String brand;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstRate = BigDecimal.ZERO;

    @Column(name = "default_cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultCostPrice = BigDecimal.ZERO;

    @Column(name = "default_selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultSellingPrice = BigDecimal.ZERO;

    @Column(name = "default_mrp", nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultMrp = BigDecimal.ZERO;

    @Column(name = "min_stock_level", nullable = false, precision = 12, scale = 3)
    private BigDecimal minStockLevel = new BigDecimal("5.000");

    @Column(name = "reorder_level", nullable = false, precision = 12, scale = 3)
    private BigDecimal reorderLevel = new BigDecimal("10.000");

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
