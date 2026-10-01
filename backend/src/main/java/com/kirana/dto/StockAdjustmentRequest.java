package com.kirana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class StockAdjustmentRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotBlank(message = "Adjustment type is required (DAMAGED, EXPIRED, LOST, THEFT, COUNTING_ERROR, MANUAL_CORRECTION)")
    private String adjustmentType;

    @NotNull(message = "Adjusted quantity is required (can be positive or negative)")
    private BigDecimal adjustedQuantity;

    @NotBlank(message = "Reason is required")
    private String reason;

    public StockAdjustmentRequest() {}

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }

    public BigDecimal getAdjustedQuantity() { return adjustedQuantity; }
    public void setAdjustedQuantity(BigDecimal adjustedQuantity) { this.adjustedQuantity = adjustedQuantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
