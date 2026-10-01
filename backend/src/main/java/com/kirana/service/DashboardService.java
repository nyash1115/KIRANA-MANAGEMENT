package com.kirana.service;

import com.kirana.dto.DashboardChartsDto;
import com.kirana.dto.DashboardSummaryDto;
import com.kirana.entity.InventoryBatch;
import com.kirana.entity.Product;
import com.kirana.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DashboardService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    public DashboardService(SaleRepository saleRepository,
                            SaleItemRepository saleItemRepository,
                            InventoryBatchRepository inventoryBatchRepository,
                            ProductRepository productRepository,
                            CustomerRepository customerRepository,
                            SupplierRepository supplierRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(Long storeId) {
        Instant startOfDay = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        List<Object[]> agg = saleRepository.getSalesAggregateMetrics(storeId, startOfDay, endOfDay);
        long todayBills = 0;
        BigDecimal todaySales = BigDecimal.ZERO;
        BigDecimal todayProfit = BigDecimal.ZERO;

        if (agg != null && !agg.isEmpty() && agg.get(0) != null) {
            Object[] row = agg.get(0);
            todayBills = row[0] != null ? ((Number) row[0]).longValue() : 0;
            todaySales = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            todayProfit = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
        }

        // Inventory values and counts
        List<InventoryBatch> allBatches = inventoryBatchRepository.findAll();
        BigDecimal invCost = BigDecimal.ZERO;
        BigDecimal invSelling = BigDecimal.ZERO;
        long outOfStock = 0;
        long nearExpiry = 0;
        long expired = 0;
        LocalDate today = LocalDate.now();
        LocalDate alertDate = today.plusDays(30);

        for (InventoryBatch b : allBatches) {
            if (storeId != null && !b.getStore().getId().equals(storeId)) continue;
            BigDecimal qty = b.getQuantity();
            if (qty.compareTo(BigDecimal.ZERO) == 0) {
                outOfStock++;
            } else {
                invCost = invCost.add(b.getCostPrice().multiply(qty));
                invSelling = invSelling.add(b.getSellingPrice().multiply(qty));
            }

            if (b.getExpiryDate() != null) {
                if (b.getExpiryDate().isBefore(today)) {
                    expired++;
                } else if (b.getExpiryDate().isBefore(alertDate)) {
                    nearExpiry++;
                }
            }
        }

        // Low stock products count
        List<Object[]> stocks = inventoryBatchRepository.getTotalStockPerProduct(storeId);
        java.util.Map<Long, BigDecimal> stockMap = new java.util.HashMap<>();
        for (Object[] r : stocks) stockMap.put((Long) r[0], (BigDecimal) r[1]);

        long lowStock = 0;
        for (Product p : productRepository.findAll()) {
            if (!p.isActive()) continue;
            BigDecimal current = stockMap.getOrDefault(p.getId(), BigDecimal.ZERO);
            if (current.compareTo(p.getMinStockLevel()) <= 0) {
                lowStock++;
            }
        }

        // Pending customer and supplier dues
        BigDecimal pendingKhata = customerRepository.findAll().stream()
                .map(c -> c.getCurrentOutstanding())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingSupplier = supplierRepository.findAll().stream()
                .map(s -> s.getOutstandingBalance())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setTodaySales(todaySales);
        dto.setTodayBillsCount(todayBills);
        dto.setTodayGrossProfit(todayProfit);
        dto.setTotalInventoryValueCost(invCost);
        dto.setTotalInventoryValueSelling(invSelling);
        dto.setLowStockCount(lowStock);
        dto.setOutOfStockCount(outOfStock);
        dto.setNearExpiryCount(nearExpiry);
        dto.setExpiredCount(expired);
        dto.setPendingCustomerKhataDues(pendingKhata);
        dto.setPendingSupplierPayables(pendingSupplier);
        return dto;
    }

    @Transactional(readOnly = true)
    public DashboardChartsDto getCharts(Long storeId, String period) {
        Instant endDate = Instant.now();
        Instant startDate;

        if ("month".equalsIgnoreCase(period)) {
            startDate = endDate.minus(30, ChronoUnit.DAYS);
        } else if ("year".equalsIgnoreCase(period)) {
            startDate = endDate.minus(365, ChronoUnit.DAYS);
        } else {
            // Default 7 days
            startDate = endDate.minus(7, ChronoUnit.DAYS);
        }

        // Top selling products
        List<Object[]> topProdRows = saleItemRepository.getTopSellingProducts(storeId, startDate, endDate, PageRequest.of(0, 5));
        List<DashboardChartsDto.TopProductPoint> topProducts = new ArrayList<>();
        for (Object[] row : topProdRows) {
            topProducts.add(new DashboardChartsDto.TopProductPoint(
                    (String) row[0],
                    (BigDecimal) row[1],
                    (BigDecimal) row[2]
            ));
        }

        // Category sales
        List<Object[]> catRows = saleItemRepository.getCategoryWiseSales(storeId, startDate, endDate);
        List<DashboardChartsDto.CategorySalesPoint> catSales = new ArrayList<>();
        for (Object[] row : catRows) {
            catSales.add(new DashboardChartsDto.CategorySalesPoint(
                    (String) row[0],
                    (BigDecimal) row[1]
            ));
        }

        // Daily trend
        List<Object[]> trendRows = saleRepository.getDailySalesTrends(storeId, startDate, endDate);
        List<DashboardChartsDto.SalesTrendPoint> trends = new ArrayList<>();
        for (Object[] row : trendRows) {
            trends.add(new DashboardChartsDto.SalesTrendPoint(
                    String.valueOf(row[0]),
                    row[1] != null ? ((Number) row[1]).longValue() : 0,
                    row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO,
                    row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO
            ));
        }

        DashboardChartsDto dto = new DashboardChartsDto();
        dto.setTopSellingProducts(topProducts);
        dto.setCategorySales(catSales);
        dto.setSalesTrend(trends);
        return dto;
    }
}
