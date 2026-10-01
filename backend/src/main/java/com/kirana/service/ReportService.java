package com.kirana.service;

import com.kirana.entity.InventoryBatch;
import com.kirana.entity.Sale;
import com.kirana.entity.SaleItem;
import com.kirana.repository.InventoryBatchRepository;
import com.kirana.repository.SaleRepository;
import com.kirana.specification.SaleSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class ReportService {

    private final SaleRepository saleRepository;
    private final InventoryBatchRepository inventoryBatchRepository;

    public ReportService(SaleRepository saleRepository, InventoryBatchRepository inventoryBatchRepository) {
        this.saleRepository = saleRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfitReport(Long storeId, Instant startDate, Instant endDate) {
        Specification<Sale> spec = SaleSpecification.withFilters(storeId, null, null, "COMPLETED", startDate, endDate);
        List<Sale> sales = saleRepository.findAll(spec);

        BigDecimal grossSales = BigDecimal.ZERO;
        BigDecimal itemDiscounts = BigDecimal.ZERO;
        BigDecimal billDiscounts = BigDecimal.ZERO;
        BigDecimal netTaxable = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalCogs = BigDecimal.ZERO;
        BigDecimal grossProfit = BigDecimal.ZERO;

        for (Sale s : sales) {
            grossSales = grossSales.add(s.getSubtotal());
            itemDiscounts = itemDiscounts.add(s.getItemDiscountTotal());
            billDiscounts = billDiscounts.add(s.getBillDiscountAmount());
            netTaxable = netTaxable.add(s.getTaxableAmount());
            totalTax = totalTax.add(s.getTotalTaxAmount());
            totalCogs = totalCogs.add(s.getTotalCogs());
            grossProfit = grossProfit.add(s.getGrossProfit());
        }

        BigDecimal marginPercent = BigDecimal.ZERO;
        if (netTaxable.compareTo(BigDecimal.ZERO) > 0) {
            marginPercent = grossProfit.multiply(new BigDecimal("100")).divide(netTaxable, 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("billsCount", sales.size());
        report.put("grossSales", grossSales);
        report.put("itemDiscounts", itemDiscounts);
        report.put("billDiscounts", billDiscounts);
        report.put("netTaxableSales", netTaxable);
        report.put("totalTaxCollected", totalTax);
        report.put("cogs", totalCogs);
        report.put("grossProfit", grossProfit);
        report.put("grossMarginPercentage", marginPercent);
        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGstSummary(Long storeId, Instant startDate, Instant endDate) {
        Specification<Sale> spec = SaleSpecification.withFilters(storeId, null, null, "COMPLETED", startDate, endDate);
        List<Sale> sales = saleRepository.findAll(spec);

        Map<String, Map<String, BigDecimal>> slabMap = new TreeMap<>();
        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;

        for (Sale s : sales) {
            for (SaleItem item : s.getItems()) {
                String slab = item.getGstRate().stripTrailingZeros().toPlainString() + "%";
                Map<String, BigDecimal> slabData = slabMap.computeIfAbsent(slab, k -> {
                    Map<String, BigDecimal> m = new HashMap<>();
                    m.put("taxable", BigDecimal.ZERO);
                    m.put("cgst", BigDecimal.ZERO);
                    m.put("sgst", BigDecimal.ZERO);
                    m.put("igst", BigDecimal.ZERO);
                    m.put("totalTax", BigDecimal.ZERO);
                    return m;
                });

                slabData.put("taxable", slabData.get("taxable").add(item.getTaxableAmount()));
                slabData.put("cgst", slabData.get("cgst").add(item.getCgstAmount()));
                slabData.put("sgst", slabData.get("sgst").add(item.getSgstAmount()));
                slabData.put("igst", slabData.get("igst").add(item.getIgstAmount()));
                slabData.put("totalTax", slabData.get("totalTax").add(item.getTotalTax()));

                totalTaxable = totalTaxable.add(item.getTaxableAmount());
                totalCgst = totalCgst.add(item.getCgstAmount());
                totalSgst = totalSgst.add(item.getSgstAmount());
                totalIgst = totalIgst.add(item.getIgstAmount());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTaxableTurnover", totalTaxable);
        result.put("totalCgst", totalCgst);
        result.put("totalSgst", totalSgst);
        result.put("totalIgst", totalIgst);
        result.put("totalTax", totalCgst.add(totalSgst).add(totalIgst));
        result.put("slabs", slabMap);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryValuation(Long storeId) {
        List<InventoryBatch> batches = inventoryBatchRepository.findAll();
        BigDecimal totalCostVal = BigDecimal.ZERO;
        BigDecimal totalSellingVal = BigDecimal.ZERO;
        BigDecimal potentialProfit = BigDecimal.ZERO;
        long activeBatchesCount = 0;

        for (InventoryBatch b : batches) {
            if (storeId != null && !b.getStore().getId().equals(storeId)) continue;
            BigDecimal q = b.getQuantity();
            if (q.compareTo(BigDecimal.ZERO) > 0) {
                activeBatchesCount++;
                BigDecimal cost = b.getCostPrice().multiply(q);
                BigDecimal sell = b.getSellingPrice().multiply(q);
                totalCostVal = totalCostVal.add(cost);
                totalSellingVal = totalSellingVal.add(sell);
            }
        }
        potentialProfit = totalSellingVal.subtract(totalCostVal);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("activeBatchesCount", activeBatchesCount);
        resp.put("inventoryCostValue", totalCostVal);
        resp.put("inventorySellingValue", totalSellingVal);
        resp.put("potentialGrossProfit", potentialProfit);
        return resp;
    }

    @Transactional(readOnly = true)
    public String exportSalesCsv(Long storeId, Instant startDate, Instant endDate) {
        Specification<Sale> spec = SaleSpecification.withFilters(storeId, null, null, "COMPLETED", startDate, endDate);
        List<Sale> sales = saleRepository.findAll(spec);

        StringBuilder sb = new StringBuilder();
        sb.append("Invoice Number,Date,Customer,Subtotal,Discounts,Taxable Amount,Tax,Total Amount,COGS,Gross Profit,Payment Method\n");

        for (Sale s : sales) {
            String cust = s.getCustomer() != null ? s.getCustomer().getName() : "Walk-in";
            String payMethods = s.getPayments().stream().map(p -> p.getPaymentMethod()).reduce((a, b) -> a + "/" + b).orElse("CASH");
            BigDecimal disc = s.getItemDiscountTotal().add(s.getBillDiscountAmount());

            sb.append(String.format("\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,\"%s\"\n",
                    s.getInvoiceNumber(),
                    s.getSaleDate().toString(),
                    cust.replace("\"", "\"\""),
                    s.getSubtotal(),
                    disc,
                    s.getTaxableAmount(),
                    s.getTotalTaxAmount(),
                    s.getTotalAmount(),
                    s.getTotalCogs(),
                    s.getGrossProfit(),
                    payMethods
            ));
        }

        return sb.toString();
    }
}
