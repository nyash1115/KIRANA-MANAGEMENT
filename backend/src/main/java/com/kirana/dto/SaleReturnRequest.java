package com.kirana.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class SaleReturnRequest {

    @NotBlank(message = "Reason for return is required")
    private String reason;

    @NotBlank(message = "Refund method is required (CASH, UPI, KHATA_CREDIT)")
    private String refundMethod;

    @NotEmpty(message = "Return items cannot be empty")
    @Valid
    private List<ReturnItemRequest> items;

    public static class ReturnItemRequest {
        @NotNull(message = "Sale item ID is required")
        private Long saleItemId;

        @NotNull(message = "Return quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than zero")
        private BigDecimal quantity;

        public ReturnItemRequest() {}

        public Long getSaleItemId() { return saleItemId; }
        public void setSaleItemId(Long saleItemId) { this.saleItemId = saleItemId; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    }

    public SaleReturnRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRefundMethod() { return refundMethod; }
    public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }

    public List<ReturnItemRequest> getItems() { return items; }
    public void setItems(List<ReturnItemRequest> items) { this.items = items; }
}
