package com.kirana.dto;

import java.math.BigDecimal;

public class DashboardSummaryDto {

    private BigDecimal todaySales;
    private long todayBillsCount;
    private BigDecimal todayGrossProfit;
    private BigDecimal totalInventoryValueCost;
    private BigDecimal totalInventoryValueSelling;
    private long lowStockCount;
    private long outOfStockCount;
    private long nearExpiryCount;
    private long expiredCount;
    private BigDecimal pendingCustomerKhataDues;
    private BigDecimal pendingSupplierPayables;

    public DashboardSummaryDto() {}

    public BigDecimal getTodaySales() { return todaySales; }
    public void setTodaySales(BigDecimal todaySales) { this.todaySales = todaySales; }

    public long getTodayBillsCount() { return todayBillsCount; }
    public void setTodayBillsCount(long todayBillsCount) { this.todayBillsCount = todayBillsCount; }

    public BigDecimal getTodayGrossProfit() { return todayGrossProfit; }
    public void setTodayGrossProfit(BigDecimal todayGrossProfit) { this.todayGrossProfit = todayGrossProfit; }

    public BigDecimal getTotalInventoryValueCost() { return totalInventoryValueCost; }
    public void setTotalInventoryValueCost(BigDecimal totalInventoryValueCost) { this.totalInventoryValueCost = totalInventoryValueCost; }

    public BigDecimal getTotalInventoryValueSelling() { return totalInventoryValueSelling; }
    public void setTotalInventoryValueSelling(BigDecimal totalInventoryValueSelling) { this.totalInventoryValueSelling = totalInventoryValueSelling; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getOutOfStockCount() { return outOfStockCount; }
    public void setOutOfStockCount(long outOfStockCount) { this.outOfStockCount = outOfStockCount; }

    public long getNearExpiryCount() { return nearExpiryCount; }
    public void setNearExpiryCount(long nearExpiryCount) { this.nearExpiryCount = nearExpiryCount; }

    public long getExpiredCount() { return expiredCount; }
    public void setExpiredCount(long expiredCount) { this.expiredCount = expiredCount; }

    public BigDecimal getPendingCustomerKhataDues() { return pendingCustomerKhataDues; }
    public void setPendingCustomerKhataDues(BigDecimal pendingCustomerKhataDues) { this.pendingCustomerKhataDues = pendingCustomerKhataDues; }

    public BigDecimal getPendingSupplierPayables() { return pendingSupplierPayables; }
    public void setPendingSupplierPayables(BigDecimal pendingSupplierPayables) { this.pendingSupplierPayables = pendingSupplierPayables; }
}
