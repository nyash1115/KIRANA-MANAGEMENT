package com.kirana.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public class BillCalculationRequest {

    private Long customerId;
    private BigDecimal billDiscountRate = BigDecimal.ZERO; // percentage e.g. 5.0 for 5%
    private BigDecimal billDiscountAmount = BigDecimal.ZERO; // or flat discount

    @NotEmpty(message = "Cart cannot be empty")
    @Valid
    private List<CartItemDto> items;

    public BillCalculationRequest() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public BigDecimal getBillDiscountRate() { return billDiscountRate; }
    public void setBillDiscountRate(BigDecimal billDiscountRate) { this.billDiscountRate = billDiscountRate; }

    public BigDecimal getBillDiscountAmount() { return billDiscountAmount; }
    public void setBillDiscountAmount(BigDecimal billDiscountAmount) { this.billDiscountAmount = billDiscountAmount; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}
