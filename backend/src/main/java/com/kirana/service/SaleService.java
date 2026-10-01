package com.kirana.service;

import com.kirana.dto.*;
import com.kirana.entity.*;
import com.kirana.exception.InsufficientStockException;
import com.kirana.exception.InvalidOperationException;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.kirana.specification.SaleSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final ProductRepository productRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final StoreRepository storeRepository;
    private final StoreSettingsRepository storeSettingsRepository;
    private final CustomerRepository customerRepository;
    private final KhataTransactionRepository khataTransactionRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public SaleService(SaleRepository saleRepository,
                       SaleItemRepository saleItemRepository,
                       SaleReturnRepository saleReturnRepository,
                       ProductRepository productRepository,
                       InventoryBatchRepository inventoryBatchRepository,
                       StoreRepository storeRepository,
                       StoreSettingsRepository storeSettingsRepository,
                       CustomerRepository customerRepository,
                       KhataTransactionRepository khataTransactionRepository,
                       AuditService auditService,
                       NotificationService notificationService) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.saleReturnRepository = saleReturnRepository;
        this.productRepository = productRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.storeRepository = storeRepository;
        this.storeSettingsRepository = storeSettingsRepository;
        this.customerRepository = customerRepository;
        this.khataTransactionRepository = khataTransactionRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public BillCalculationResponse calculateBill(Long storeId, BillCalculationRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        StoreSettings settings = storeSettingsRepository.findByStoreId(storeId)
                .orElse(new StoreSettings());

        boolean isIgst = settings.isEnableIgst();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal itemDiscountTotal = BigDecimal.ZERO;
        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalCogs = BigDecimal.ZERO;

        List<BillCalculationResponse.CalculatedItemDto> calcItems = new ArrayList<>();

        for (CartItemDto item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            InventoryBatch batch = null;
            if (item.getBatchId() != null) {
                batch = inventoryBatchRepository.findById(item.getBatchId()).orElse(null);
            }
            if (batch == null) {
                List<InventoryBatch> fefo = inventoryBatchRepository.findFefoBatches(storeId, product.getId(), LocalDate.now());
                if (!fefo.isEmpty()) {
                    batch = fefo.get(0);
                }
            }

            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() :
                    (batch != null ? batch.getSellingPrice() : product.getDefaultSellingPrice());
            BigDecimal costPrice = batch != null ? batch.getCostPrice() : product.getDefaultCostPrice();

            BigDecimal gross = unitPrice.multiply(item.getQuantity());
            BigDecimal itemDisc = item.getItemDiscount() != null ? item.getItemDiscount() : BigDecimal.ZERO;
            BigDecimal taxable = gross.subtract(itemDisc);

            BigDecimal gstRate = product.getGstRate();
            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;

            if (isIgst) {
                igst = taxable.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                BigDecimal halfRate = gstRate.divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
                cgst = taxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                sgst = taxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            BigDecimal lineTax = cgst.add(sgst).add(igst);
            BigDecimal lineTotal = taxable.add(lineTax);
            BigDecimal lineCogs = costPrice.multiply(item.getQuantity());
            BigDecimal lineProfit = taxable.subtract(lineCogs);

            subtotal = subtotal.add(gross);
            itemDiscountTotal = itemDiscountTotal.add(itemDisc);
            totalTaxable = totalTaxable.add(taxable);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            totalIgst = totalIgst.add(igst);
            totalCogs = totalCogs.add(lineCogs);

            BillCalculationResponse.CalculatedItemDto cItem = new BillCalculationResponse.CalculatedItemDto();
            cItem.setProductId(product.getId());
            cItem.setProductName(product.getName());
            cItem.setProductBarcode(product.getBarcode());
            if (batch != null) {
                cItem.setBatchId(batch.getId());
                cItem.setBatchNumber(batch.getBatchNumber());
            }
            cItem.setQuantity(item.getQuantity());
            cItem.setUnitPrice(unitPrice);
            cItem.setCostPrice(costPrice);
            cItem.setItemDiscount(itemDisc);
            cItem.setTaxableAmount(taxable);
            cItem.setGstRate(gstRate);
            cItem.setCgstAmount(cgst);
            cItem.setSgstAmount(sgst);
            cItem.setIgstAmount(igst);
            cItem.setTotalTax(lineTax);
            cItem.setLineTotal(lineTotal);
            cItem.setLineProfit(lineProfit);
            calcItems.add(cItem);
        }

        BigDecimal billDiscountAmount = request.getBillDiscountAmount() != null ? request.getBillDiscountAmount() : BigDecimal.ZERO;
        if (request.getBillDiscountRate() != null && request.getBillDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            billDiscountAmount = totalTaxable.multiply(request.getBillDiscountRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        BigDecimal finalTaxable = totalTaxable.subtract(billDiscountAmount);
        BigDecimal totalTax = totalCgst.add(totalSgst).add(totalIgst);
        BigDecimal rawTotal = finalTaxable.add(totalTax);
        BigDecimal grandTotal = rawTotal.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOff = grandTotal.subtract(rawTotal);

        BigDecimal estimatedProfit = finalTaxable.subtract(totalCogs);

        BillCalculationResponse resp = new BillCalculationResponse();
        resp.setSubtotal(subtotal);
        resp.setItemDiscountTotal(itemDiscountTotal);
        resp.setBillDiscountRate(request.getBillDiscountRate() != null ? request.getBillDiscountRate() : BigDecimal.ZERO);
        resp.setBillDiscountAmount(billDiscountAmount);
        resp.setTaxableAmount(finalTaxable);
        resp.setCgstAmount(totalCgst);
        resp.setSgstAmount(totalSgst);
        resp.setIgstAmount(totalIgst);
        resp.setTotalTaxAmount(totalTax);
        resp.setRoundOff(roundOff);
        resp.setGrandTotal(grandTotal);
        resp.setEstimatedProfit(estimatedProfit);
        resp.setItems(calcItems);
        return resp;
    }

    @Transactional
    public SaleResponse checkout(Long storeId, CheckoutRequest request, User cashier) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        StoreSettings settings = storeSettingsRepository.findByStoreId(storeId)
                .orElse(new StoreSettings());

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));
        }

        boolean allowNeg = settings.isAllowNegativeStock();
        boolean isIgst = settings.isEnableIgst();

        // Sequential invoice numbering
        Long nextSeq = store.getInvoiceNextSeq();
        String invoiceNumber = String.format("%s-%d-%05d", store.getInvoicePrefix(), LocalDate.now().getYear(), nextSeq);
        store.setInvoiceNextSeq(nextSeq + 1);
        storeRepository.save(store);

        Sale sale = new Sale();
        sale.setStore(store);
        sale.setCustomer(customer);
        sale.setCashier(cashier);
        sale.setInvoiceNumber(invoiceNumber);
        sale.setSaleDate(Instant.now());
        sale.setNotes(request.getNotes());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal itemDiscountTotal = BigDecimal.ZERO;
        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalCogs = BigDecimal.ZERO;

        List<SaleItem> saleItems = new ArrayList<>();

        for (CartItemDto cartItem : request.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartItem.getProductId()));

            // Select batch via FEFO with pessimistic write lock
            InventoryBatch batch = null;
            if (cartItem.getBatchId() != null) {
                batch = inventoryBatchRepository.findByIdWithLock(cartItem.getBatchId()).orElse(null);
            }
            if (batch == null) {
                List<InventoryBatch> fefoBatches = inventoryBatchRepository.findFefoBatches(storeId, product.getId(), LocalDate.now());
                if (!fefoBatches.isEmpty()) {
                    batch = inventoryBatchRepository.findByIdWithLock(fefoBatches.get(0).getId()).orElse(null);
                }
            }

            if (batch == null && !allowNeg) {
                throw new InsufficientStockException("Out of stock: No available batch for product '" + product.getName() + "'");
            }

            BigDecimal reqQty = cartItem.getQuantity();
            if (batch != null && batch.getQuantity().compareTo(reqQty) < 0 && !allowNeg) {
                throw new InsufficientStockException("Insufficient stock for product '" + product.getName() + "'. Available: " + batch.getQuantity() + ", Requested: " + reqQty);
            }

            // Deduct stock
            BigDecimal costPrice = product.getDefaultCostPrice();
            if (batch != null) {
                BigDecimal remaining = batch.getQuantity().subtract(reqQty);
                batch.setQuantity(remaining.max(BigDecimal.ZERO));
                if (batch.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    batch.setStatus("DEPLETED");
                }
                inventoryBatchRepository.save(batch);
                costPrice = batch.getCostPrice();

                // Trigger low stock notification if batch reaches low level
                if (batch.getQuantity().compareTo(product.getMinStockLevel()) <= 0) {
                    notificationService.createNotification(store, "LOW_STOCK", "Low Stock Alert: " + product.getName(),
                            "Stock for " + product.getName() + " (Batch " + batch.getBatchNumber() + ") is down to " + batch.getQuantity() + " " + product.getUnit().getCode(),
                            "WARNING", "Product", String.valueOf(product.getId()));
                }
            }

            BigDecimal unitPrice = cartItem.getUnitPrice() != null ? cartItem.getUnitPrice() :
                    (batch != null ? batch.getSellingPrice() : product.getDefaultSellingPrice());

            BigDecimal lineGross = unitPrice.multiply(reqQty);
            BigDecimal itemDisc = cartItem.getItemDiscount() != null ? cartItem.getItemDiscount() : BigDecimal.ZERO;
            BigDecimal lineTaxable = lineGross.subtract(itemDisc);

            BigDecimal gstRate = product.getGstRate();
            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;

            if (isIgst) {
                igst = lineTaxable.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                BigDecimal halfRate = gstRate.divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
                cgst = lineTaxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                sgst = lineTaxable.multiply(halfRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            BigDecimal lineTax = cgst.add(sgst).add(igst);
            BigDecimal lineTotal = lineTaxable.add(lineTax);
            BigDecimal lineCogs = costPrice.multiply(reqQty);
            BigDecimal lineProfit = lineTaxable.subtract(lineCogs);

            subtotal = subtotal.add(lineGross);
            itemDiscountTotal = itemDiscountTotal.add(itemDisc);
            totalTaxable = totalTaxable.add(lineTaxable);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            totalIgst = totalIgst.add(igst);
            totalCogs = totalCogs.add(lineCogs);

            SaleItem si = new SaleItem();
            si.setSale(sale);
            si.setProduct(product);
            si.setInventoryBatch(batch);
            si.setQuantity(reqQty);
            si.setUnitPrice(unitPrice);
            si.setCostPrice(costPrice);
            si.setDiscountAmount(itemDisc);
            si.setTaxableAmount(lineTaxable);
            si.setGstRate(gstRate);
            si.setCgstAmount(cgst);
            si.setSgstAmount(sgst);
            si.setIgstAmount(igst);
            si.setTotalTax(lineTax);
            si.setLineTotal(lineTotal);
            si.setLineCogs(lineCogs);
            si.setLineProfit(lineProfit);
            saleItems.add(si);
        }

        BigDecimal billDiscAmount = request.getBillDiscountAmount() != null ? request.getBillDiscountAmount() : BigDecimal.ZERO;
        if (request.getBillDiscountRate() != null && request.getBillDiscountRate().compareTo(BigDecimal.ZERO) > 0) {
            billDiscAmount = totalTaxable.multiply(request.getBillDiscountRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        BigDecimal netTaxable = totalTaxable.subtract(billDiscAmount);
        BigDecimal totalTax = totalCgst.add(totalSgst).add(totalIgst);
        BigDecimal rawTotal = netTaxable.add(totalTax);
        BigDecimal grandTotal = rawTotal.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOff = grandTotal.subtract(rawTotal);
        BigDecimal grossProfit = netTaxable.subtract(totalCogs);

        sale.setSubtotal(subtotal);
        sale.setItemDiscountTotal(itemDiscountTotal);
        sale.setBillDiscountRate(request.getBillDiscountRate() != null ? request.getBillDiscountRate() : BigDecimal.ZERO);
        sale.setBillDiscountAmount(billDiscAmount);
        sale.setTaxableAmount(netTaxable);
        sale.setCgstAmount(totalCgst);
        sale.setSgstAmount(totalSgst);
        sale.setIgstAmount(totalIgst);
        sale.setTotalTaxAmount(totalTax);
        sale.setRoundOff(roundOff);
        sale.setTotalAmount(grandTotal);
        sale.setTotalCogs(totalCogs);
        sale.setGrossProfit(grossProfit);
        sale.setStatus("COMPLETED");

        // Process payments
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal udhaarPortion = BigDecimal.ZERO;
        List<SalePayment> payments = new ArrayList<>();

        for (PaymentItemDto payDto : request.getPayments()) {
            SalePayment sp = new SalePayment();
            sp.setSale(sale);
            sp.setPaymentMethod(payDto.getPaymentMethod());
            sp.setAmount(payDto.getAmount());
            sp.setTransactionRef(payDto.getTransactionRef());
            sp.setNotes(payDto.getNotes());
            payments.add(sp);

            if ("UDHAAR".equalsIgnoreCase(payDto.getPaymentMethod()) || "CREDIT".equalsIgnoreCase(payDto.getPaymentMethod())) {
                udhaarPortion = udhaarPortion.add(payDto.getAmount());
            } else {
                totalPaid = totalPaid.add(payDto.getAmount());
            }
        }

        sale.setPaidAmount(totalPaid);
        if (udhaarPortion.compareTo(BigDecimal.ZERO) > 0) {
            sale.setPaymentStatus(totalPaid.compareTo(BigDecimal.ZERO) > 0 ? "PARTIAL" : "CREDIT");
        } else {
            sale.setPaymentStatus("PAID");
        }

        sale.setItems(saleItems);
        sale.setPayments(payments);
        Sale savedSale = saleRepository.save(sale);

        // Update Customer Khata if Udhaar was used
        if (udhaarPortion.compareTo(BigDecimal.ZERO) > 0) {
            if (customer == null) {
                throw new InvalidOperationException("Customer must be selected for Udhaar (Credit) purchases");
            }
            BigDecimal newBalance = customer.getCurrentOutstanding().add(udhaarPortion);
            if (customer.getCreditLimit() != null && newBalance.compareTo(customer.getCreditLimit()) > 0) {
                throw new InvalidOperationException("Credit limit exceeded! Limit: " + customer.getCreditLimit() + ", Current: " + customer.getCurrentOutstanding() + ", Attempted: " + udhaarPortion);
            }
            customer.setCurrentOutstanding(newBalance);
            customerRepository.save(customer);

            KhataTransaction kt = new KhataTransaction();
            kt.setStore(store);
            kt.setCustomer(customer);
            kt.setType("DEBIT_SALE");
            kt.setReferenceType("SALE");
            kt.setReferenceId(savedSale.getInvoiceNumber());
            kt.setAmount(udhaarPortion);
            kt.setBalanceAfter(newBalance);
            kt.setNotes("Bill " + savedSale.getInvoiceNumber() + " credit sale");
            kt.setCreatedBy(cashier);
            khataTransactionRepository.save(kt);
        }

        auditService.logAction(store.getBusiness(), store, cashier, "SALE", "Sale", String.valueOf(savedSale.getId()),
                null, "Invoice: " + savedSale.getInvoiceNumber() + ", Total: " + savedSale.getTotalAmount(), null);

        return mapToSaleResponse(savedSale, settings);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SaleResponse> getSales(Long storeId, String invoiceNumber, Long customerId, String status, Instant startDate, Instant endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Sale> spec = SaleSpecification.withFilters(storeId, invoiceNumber, customerId, status, startDate, endDate);
        Page<Sale> salePage = saleRepository.findAll(spec, pageable);
        StoreSettings settings = storeSettingsRepository.findByStoreId(storeId).orElse(new StoreSettings());
        List<SaleResponse> list = salePage.getContent().stream().map(s -> mapToSaleResponse(s, settings)).toList();
        return new PagedResponse<>(list, salePage.getNumber(), salePage.getSize(), salePage.getTotalElements(), salePage.getTotalPages(), salePage.isLast());
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        Sale s = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        StoreSettings settings = storeSettingsRepository.findByStoreId(s.getStore().getId()).orElse(new StoreSettings());
        return mapToSaleResponse(s, settings);
    }

    @Transactional
    public SaleResponse cancelSale(Long saleId, String reason, User user) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + saleId));

        if ("CANCELLED".equals(sale.getStatus())) {
            throw new InvalidOperationException("Sale is already cancelled");
        }

        // Restock inventory batches
        for (SaleItem item : sale.getItems()) {
            if (item.getInventoryBatch() != null) {
                InventoryBatch batch = inventoryBatchRepository.findByIdWithLock(item.getInventoryBatch().getId()).orElse(null);
                if (batch != null) {
                    batch.setQuantity(batch.getQuantity().add(item.getQuantity()));
                    batch.setStatus("ACTIVE");
                    inventoryBatchRepository.save(batch);
                }
            }
        }

        // If customer was billed on credit, reverse Khata
        BigDecimal creditAmount = sale.getTotalAmount().subtract(sale.getPaidAmount());
        if (creditAmount.compareTo(BigDecimal.ZERO) > 0 && sale.getCustomer() != null) {
            Customer c = sale.getCustomer();
            BigDecimal newBal = c.getCurrentOutstanding().subtract(creditAmount);
            c.setCurrentOutstanding(newBal);
            customerRepository.save(c);

            KhataTransaction kt = new KhataTransaction();
            kt.setStore(sale.getStore());
            kt.setCustomer(c);
            kt.setType("ADJUSTMENT");
            kt.setReferenceType("SALE_CANCEL");
            kt.setReferenceId(sale.getInvoiceNumber());
            kt.setAmount(creditAmount);
            kt.setBalanceAfter(newBal);
            kt.setNotes("Cancelled invoice " + sale.getInvoiceNumber() + " credit reversal");
            kt.setCreatedBy(user);
            khataTransactionRepository.save(kt);
        }

        sale.setStatus("CANCELLED");
        sale.setCancelReason(reason);
        sale.setCancelledAt(Instant.now());
        sale.setCancelledBy(user);
        saleRepository.save(sale);

        auditService.logAction(sale.getStore().getBusiness(), sale.getStore(), user, "CANCEL_SALE", "Sale", String.valueOf(sale.getId()),
                "Invoice: " + sale.getInvoiceNumber(), "Status: CANCELLED, Reason: " + reason, null);

        StoreSettings settings = storeSettingsRepository.findByStoreId(sale.getStore().getId()).orElse(new StoreSettings());
        return mapToSaleResponse(sale, settings);
    }

    @Transactional
    public SaleResponse processReturn(Long saleId, SaleReturnRequest request, User user) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + saleId));

        if ("CANCELLED".equals(sale.getStatus())) {
            throw new InvalidOperationException("Cannot return items from a cancelled sale");
        }

        BigDecimal totalRefund = BigDecimal.ZERO;
        BigDecimal cogsReversal = BigDecimal.ZERO;

        SaleReturn sr = new SaleReturn();
        sr.setStore(sale.getStore());
        sr.setSale(sale);
        sr.setCustomer(sale.getCustomer());
        sr.setReturnNumber("RET-" + System.currentTimeMillis());
        sr.setReturnDate(Instant.now());
        sr.setRefundMethod(request.getRefundMethod());
        sr.setReason(request.getReason());
        sr.setCreatedBy(user);

        List<SaleReturnItem> returnItems = new ArrayList<>();

        for (SaleReturnRequest.ReturnItemRequest reqItem : request.getItems()) {
            SaleItem saleItem = saleItemRepository.findById(reqItem.getSaleItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sale item not found with ID: " + reqItem.getSaleItemId()));

            BigDecimal maxReturnable = saleItem.getQuantity().subtract(saleItem.getReturnedQuantity());
            if (reqItem.getQuantity().compareTo(maxReturnable) > 0) {
                throw new InvalidOperationException("Cannot return " + reqItem.getQuantity() + " units. Maximum returnable: " + maxReturnable);
            }

            saleItem.setReturnedQuantity(saleItem.getReturnedQuantity().add(reqItem.getQuantity()));
            saleItemRepository.save(saleItem);

            // Restock batch
            if (saleItem.getInventoryBatch() != null) {
                InventoryBatch batch = inventoryBatchRepository.findByIdWithLock(saleItem.getInventoryBatch().getId()).orElse(null);
                if (batch != null) {
                    batch.setQuantity(batch.getQuantity().add(reqItem.getQuantity()));
                    batch.setStatus("ACTIVE");
                    inventoryBatchRepository.save(batch);
                }
            }

            BigDecimal unitPrice = saleItem.getUnitPrice();
            BigDecimal costPrice = saleItem.getCostPrice();
            BigDecimal refundAmt = unitPrice.multiply(reqItem.getQuantity());
            BigDecimal itemCogsRev = costPrice.multiply(reqItem.getQuantity());

            totalRefund = totalRefund.add(refundAmt);
            cogsReversal = cogsReversal.add(itemCogsRev);

            SaleReturnItem sri = new SaleReturnItem();
            sri.setSaleReturn(sr);
            sri.setSaleItem(saleItem);
            sri.setProduct(saleItem.getProduct());
            sri.setInventoryBatch(saleItem.getInventoryBatch());
            sri.setQuantity(reqItem.getQuantity());
            sri.setUnitPrice(unitPrice);
            sri.setCostPrice(costPrice);
            sri.setTaxRefund(BigDecimal.ZERO);
            sri.setTotalRefund(refundAmt);
            returnItems.add(sri);
        }

        sr.setSubtotalRefund(totalRefund);
        sr.setTotalRefund(totalRefund);
        sr.setCogsReversal(cogsReversal);
        sr.setProfitReversal(totalRefund.subtract(cogsReversal));
        sr.setItems(returnItems);
        saleReturnRepository.save(sr);

        // Update customer Khata if refund method is KHATA_CREDIT
        if ("KHATA_CREDIT".equalsIgnoreCase(request.getRefundMethod()) && sale.getCustomer() != null) {
            Customer c = sale.getCustomer();
            BigDecimal newBal = c.getCurrentOutstanding().subtract(totalRefund);
            c.setCurrentOutstanding(newBal);
            customerRepository.save(c);

            KhataTransaction kt = new KhataTransaction();
            kt.setStore(sale.getStore());
            kt.setCustomer(c);
            kt.setType("RETURN_CREDIT");
            kt.setReferenceType("SALE_RETURN");
            kt.setReferenceId(sr.getReturnNumber());
            kt.setAmount(totalRefund);
            kt.setBalanceAfter(newBal);
            kt.setNotes("Return " + sr.getReturnNumber() + " credit against invoice " + sale.getInvoiceNumber());
            kt.setCreatedBy(user);
            khataTransactionRepository.save(kt);
        }

        // Check if all items fully returned
        boolean allReturned = sale.getItems().stream()
                .allMatch(item -> item.getReturnedQuantity().compareTo(item.getQuantity()) >= 0);
        sale.setStatus(allReturned ? "FULLY_RETURNED" : "PARTIALLY_RETURNED");
        saleRepository.save(sale);

        auditService.logAction(sale.getStore().getBusiness(), sale.getStore(), user, "SALE_RETURN", "SaleReturn", String.valueOf(sr.getId()),
                "Invoice: " + sale.getInvoiceNumber(), "Refund: " + totalRefund + " via " + request.getRefundMethod(), null);

        StoreSettings settings = storeSettingsRepository.findByStoreId(sale.getStore().getId()).orElse(new StoreSettings());
        return mapToSaleResponse(sale, settings);
    }

    private SaleResponse mapToSaleResponse(Sale s, StoreSettings settings) {
        SaleResponse resp = new SaleResponse();
        resp.setId(s.getId());
        resp.setStoreId(s.getStore().getId());
        resp.setStoreName(s.getStore().getName());
        resp.setStoreCode(s.getStore().getCode());
        resp.setStoreAddress(s.getStore().getAddress());
        resp.setStoreCity(s.getStore().getCity());
        resp.setStoreState(s.getStore().getState());
        resp.setStorePincode(s.getStore().getPincode());
        resp.setStorePhone(s.getStore().getPhone());
        resp.setStoreGstin(s.getStore().getGstin());

        if (settings != null) {
            resp.setInvoiceFooterMessage(settings.getInvoiceFooterMessage());
            resp.setInvoiceTerms(settings.getInvoiceTerms());
            resp.setThermalPaperWidthMm(settings.getThermalPaperWidthMm());
        }

        if (s.getCustomer() != null) {
            resp.setCustomerId(s.getCustomer().getId());
            resp.setCustomerName(s.getCustomer().getName());
            resp.setCustomerPhone(s.getCustomer().getPhone());
            resp.setCustomerCurrentOutstanding(s.getCustomer().getCurrentOutstanding());
        }

        resp.setCashierName(s.getCashier() != null ? s.getCashier().getFullName() : "Cashier");
        resp.setInvoiceNumber(s.getInvoiceNumber());
        resp.setSaleDate(s.getSaleDate());

        resp.setSubtotal(s.getSubtotal());
        resp.setItemDiscountTotal(s.getItemDiscountTotal());
        resp.setBillDiscountRate(s.getBillDiscountRate());
        resp.setBillDiscountAmount(s.getBillDiscountAmount());
        resp.setTaxableAmount(s.getTaxableAmount());
        resp.setCgstAmount(s.getCgstAmount());
        resp.setSgstAmount(s.getSgstAmount());
        resp.setIgstAmount(s.getIgstAmount());
        resp.setTotalTaxAmount(s.getTotalTaxAmount());
        resp.setRoundOff(s.getRoundOff());
        resp.setTotalAmount(s.getTotalAmount());
        resp.setTotalCogs(s.getTotalCogs());
        resp.setGrossProfit(s.getGrossProfit());
        resp.setPaidAmount(s.getPaidAmount());
        resp.setStatus(s.getStatus());
        resp.setPaymentStatus(s.getPaymentStatus());
        resp.setNotes(s.getNotes());
        resp.setCancelReason(s.getCancelReason());

        List<SaleResponse.SaleItemResponseDto> itemDtos = s.getItems().stream().map(item -> {
            SaleResponse.SaleItemResponseDto idto = new SaleResponse.SaleItemResponseDto();
            idto.setId(item.getId());
            idto.setProductId(item.getProduct().getId());
            idto.setProductName(item.getProduct().getName());
            idto.setProductBarcode(item.getProduct().getBarcode());
            idto.setProductSku(item.getProduct().getSku());
            idto.setUnitName(item.getProduct().getUnit().getName());
            if (item.getInventoryBatch() != null) {
                idto.setBatchId(item.getInventoryBatch().getId());
                idto.setBatchNumber(item.getInventoryBatch().getBatchNumber());
            }
            idto.setQuantity(item.getQuantity());
            idto.setUnitPrice(item.getUnitPrice());
            idto.setCostPrice(item.getCostPrice());
            idto.setDiscountAmount(item.getDiscountAmount());
            idto.setTaxableAmount(item.getTaxableAmount());
            idto.setGstRate(item.getGstRate());
            idto.setCgstAmount(item.getCgstAmount());
            idto.setSgstAmount(item.getSgstAmount());
            idto.setIgstAmount(item.getIgstAmount());
            idto.setTotalTax(item.getTotalTax());
            idto.setLineTotal(item.getLineTotal());
            idto.setLineProfit(item.getLineProfit());
            idto.setReturnedQuantity(item.getReturnedQuantity());
            return idto;
        }).toList();

        List<SaleResponse.SalePaymentResponseDto> payDtos = s.getPayments().stream().map(p -> {
            SaleResponse.SalePaymentResponseDto pdto = new SaleResponse.SalePaymentResponseDto();
            pdto.setId(p.getId());
            pdto.setPaymentMethod(p.getPaymentMethod());
            pdto.setAmount(p.getAmount());
            pdto.setTransactionRef(p.getTransactionRef());
            pdto.setNotes(p.getNotes());
            return pdto;
        }).toList();

        resp.setItems(itemDtos);
        resp.setPayments(payDtos);
        return resp;
    }
}
