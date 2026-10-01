package com.kirana.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale_returns")
public class SaleReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "return_number", nullable = false, unique = true, length = 50)
    private String returnNumber;

    @Column(name = "return_date", nullable = false)
    private Instant returnDate = Instant.now();

    @Column(name = "subtotal_refund", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalRefund;

    @Column(name = "tax_refund", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxRefund = BigDecimal.ZERO;

    @Column(name = "total_refund", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRefund;

    @Column(name = "cogs_reversal", nullable = false, precision = 12, scale = 2)
    private BigDecimal cogsReversal = BigDecimal.ZERO;

    @Column(name = "profit_reversal", nullable = false, precision = 12, scale = 2)
    private BigDecimal profitReversal = BigDecimal.ZERO;

    @Column(name = "refund_method", nullable = false, length = 30)
    private String refundMethod; // CASH, UPI, KHATA_CREDIT

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleReturnItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SaleReturn() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }

    public Instant getReturnDate() { return returnDate; }
    public void setReturnDate(Instant returnDate) { this.returnDate = returnDate; }

    public BigDecimal getSubtotalRefund() { return subtotalRefund; }
    public void setSubtotalRefund(BigDecimal subtotalRefund) { this.subtotalRefund = subtotalRefund; }

    public BigDecimal getTaxRefund() { return taxRefund; }
    public void setTaxRefund(BigDecimal taxRefund) { this.taxRefund = taxRefund; }

    public BigDecimal getTotalRefund() { return totalRefund; }
    public void setTotalRefund(BigDecimal totalRefund) { this.totalRefund = totalRefund; }

    public BigDecimal getCogsReversal() { return cogsReversal; }
    public void setCogsReversal(BigDecimal cogsReversal) { this.cogsReversal = cogsReversal; }

    public BigDecimal getProfitReversal() { return profitReversal; }
    public void setProfitReversal(BigDecimal profitReversal) { this.profitReversal = profitReversal; }

    public String getRefundMethod() { return refundMethod; }
    public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<SaleReturnItem> getItems() { return items; }
    public void setItems(List<SaleReturnItem> items) { this.items = items; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
