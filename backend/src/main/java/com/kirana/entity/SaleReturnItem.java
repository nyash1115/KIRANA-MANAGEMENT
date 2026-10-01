package com.kirana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_return_items")
public class SaleReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_return_id", nullable = false)
    private SaleReturn saleReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItem saleItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_batch_id")
    private InventoryBatch inventoryBatch;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "tax_refund", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxRefund = BigDecimal.ZERO;

    @Column(name = "total_refund", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRefund;

    public SaleReturnItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SaleReturn getSaleReturn() { return saleReturn; }
    public void setSaleReturn(SaleReturn saleReturn) { this.saleReturn = saleReturn; }

    public SaleItem getSaleItem() { return saleItem; }
    public void setSaleItem(SaleItem saleItem) { this.saleItem = saleItem; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public InventoryBatch getInventoryBatch() { return inventoryBatch; }
    public void setInventoryBatch(InventoryBatch inventoryBatch) { this.inventoryBatch = inventoryBatch; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getTaxRefund() { return taxRefund; }
    public void setTaxRefund(BigDecimal taxRefund) { this.taxRefund = taxRefund; }

    public BigDecimal getTotalRefund() { return totalRefund; }
    public void setTotalRefund(BigDecimal totalRefund) { this.totalRefund = totalRefund; }
}
