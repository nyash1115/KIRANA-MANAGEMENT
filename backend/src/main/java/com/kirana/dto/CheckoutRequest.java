package com.kirana.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public class CheckoutRequest {

    private Long customerId;
    private BigDecimal billDiscountRate = BigDecimal.ZERO;
    private BigDecimal billDiscountAmount = BigDecimal.ZERO;
    private String notes;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<CartItemDto> items;

    @NotEmpty(message = "Payments list cannot be empty")
    @Valid
    private List<PaymentItemDto> payments;

    public CheckoutRequest() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public BigDecimal getBillDiscountRate() { return billDiscountRate; }
    public void setBillDiscountRate(BigDecimal billDiscountRate) { this.billDiscountRate = billDiscountRate; }

    public BigDecimal getBillDiscountAmount() { return billDiscountAmount; }
    public void setBillDiscountAmount(BigDecimal billDiscountAmount) { this.billDiscountAmount = billDiscountAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }

    public List<PaymentItemDto> getPayments() { return payments; }
    public void setPayments(List<PaymentItemDto> payments) { this.payments = payments; }
}
