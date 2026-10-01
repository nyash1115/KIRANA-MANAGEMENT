package com.kirana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_adjustments")
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_batch_id", nullable = false)
    private InventoryBatch inventoryBatch;

    @Column(name = "adjustment_type", nullable = false, length = 40)
    private String adjustmentType; // DAMAGED, EXPIRED, LOST, THEFT, COUNTING_ERROR, MANUAL_CORRECTION

    @Column(name = "quantity_before", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityBefore;

    @Column(name = "adjusted_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal adjustedQuantity;

    @Column(name = "quantity_after", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityAfter;

    @Column(name = "cost_impact", nullable = false, precision = 12, scale = 2)
    private BigDecimal costImpact;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by")
    private User adjustedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public StockAdjustment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public InventoryBatch getInventoryBatch() { return inventoryBatch; }
    public void setInventoryBatch(InventoryBatch inventoryBatch) { this.inventoryBatch = inventoryBatch; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }

    public BigDecimal getQuantityBefore() { return quantityBefore; }
    public void setQuantityBefore(BigDecimal quantityBefore) { this.quantityBefore = quantityBefore; }

    public BigDecimal getAdjustedQuantity() { return adjustedQuantity; }
    public void setAdjustedQuantity(BigDecimal adjustedQuantity) { this.adjustedQuantity = adjustedQuantity; }

    public BigDecimal getQuantityAfter() { return quantityAfter; }
    public void setQuantityAfter(BigDecimal quantityAfter) { this.quantityAfter = quantityAfter; }

    public BigDecimal getCostImpact() { return costImpact; }
    public void setCostImpact(BigDecimal costImpact) { this.costImpact = costImpact; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getAdjustedBy() { return adjustedBy; }
    public void setAdjustedBy(User adjustedBy) { this.adjustedBy = adjustedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
