package com.kirana.service;

import com.kirana.dto.PagedResponse;
import com.kirana.dto.PurchaseItemRequest;
import com.kirana.dto.PurchaseRequest;
import com.kirana.dto.PurchaseResponse;
import com.kirana.entity.*;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final StoreRepository storeRepository;
    private final AuditService auditService;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           SupplierRepository supplierRepository,
                           ProductRepository productRepository,
                           InventoryBatchRepository inventoryBatchRepository,
                           StoreRepository storeRepository,
                           AuditService auditService) {
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.storeRepository = storeRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PurchaseResponse createPurchase(Long storeId, PurchaseRequest request, User currentUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.getSupplierId()));

        Purchase purchase = new Purchase();
        purchase.setStore(store);
        purchase.setSupplier(supplier);
        purchase.setInvoiceNumber(request.getInvoiceNumber());
        purchase.setPurchaseDate(request.getPurchaseDate());
        purchase.setPaymentMethod(request.getPaymentMethod());
        purchase.setNotes(request.getNotes());
        purchase.setCreatedBy(currentUser);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        List<PurchaseItem> items = new ArrayList<>();

        for (PurchaseItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.getProductId()));

            // Find or create InventoryBatch
            InventoryBatch batch = new InventoryBatch();
            batch.setStore(store);
            batch.setProduct(product);
            batch.setBatchNumber(itemReq.getBatchNumber());
            batch.setQuantity(itemReq.getQuantity());
            batch.setInitialQuantity(itemReq.getQuantity());
            batch.setCostPrice(itemReq.getCostPrice());
            batch.setSellingPrice(itemReq.getSellingPrice() != null ? itemReq.getSellingPrice() : product.getDefaultSellingPrice());
            batch.setMrp(itemReq.getMrp() != null ? itemReq.getMrp() : product.getDefaultMrp());
            batch.setExpiryDate(itemReq.getExpiryDate());
            batch.setStatus("ACTIVE");
            InventoryBatch savedBatch = inventoryBatchRepository.save(batch);

            // Calculate Item Tax & Total
            BigDecimal lineGross = itemReq.getCostPrice().multiply(itemReq.getQuantity());
            BigDecimal gstRate = itemReq.getGstRate() != null ? itemReq.getGstRate() : product.getGstRate();
            BigDecimal gstAmount = lineGross.multiply(gstRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal lineTotal = lineGross.add(gstAmount);

            subtotal = subtotal.add(lineGross);
            totalTax = totalTax.add(gstAmount);

            PurchaseItem pi = new PurchaseItem();
            pi.setPurchase(purchase);
            pi.setProduct(product);
            pi.setInventoryBatch(savedBatch);
            pi.setBatchNumber(itemReq.getBatchNumber());
            pi.setExpiryDate(itemReq.getExpiryDate());
            pi.setQuantity(itemReq.getQuantity());
            pi.setCostPrice(itemReq.getCostPrice());
            pi.setGstRate(gstRate);
            pi.setGstAmount(gstAmount);
            pi.setTotalAmount(lineTotal);
            items.add(pi);
        }

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.add(totalTax).subtract(discount);
        BigDecimal paid = request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO;

        purchase.setSubtotal(subtotal);
        purchase.setTaxAmount(totalTax);
        purchase.setDiscountAmount(discount);
        purchase.setTotalAmount(grandTotal);
        purchase.setPaidAmount(paid);

        if (paid.compareTo(grandTotal) >= 0) {
            purchase.setPaymentStatus("PAID");
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            purchase.setPaymentStatus("PARTIAL");
        } else {
            purchase.setPaymentStatus("UNPAID");
        }

        purchase.setItems(items);
        Purchase saved = purchaseRepository.save(purchase);

        // Update supplier outstanding balance
        BigDecimal unpaidPortion = grandTotal.subtract(paid);
        if (unpaidPortion.compareTo(BigDecimal.ZERO) > 0) {
            supplier.setOutstandingBalance(supplier.getOutstandingBalance().add(unpaidPortion));
            supplierRepository.save(supplier);
        }

        auditService.logAction(store.getBusiness(), store, currentUser, "PURCHASE", "Purchase", String.valueOf(saved.getId()), null, "Invoice: " + saved.getInvoiceNumber() + ", Total: " + saved.getTotalAmount(), null);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PurchaseResponse> getPurchases(Long storeId, Long supplierId, LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Purchase> purchasePage = purchaseRepository.findPurchasesWithFilters(storeId, supplierId, startDate, endDate, pageable);
        List<PurchaseResponse> list = purchasePage.getContent().stream().map(this::mapToResponse).toList();
        return new PagedResponse<>(list, purchasePage.getNumber(), purchasePage.getSize(), purchasePage.getTotalElements(), purchasePage.getTotalPages(), purchasePage.isLast());
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(Long id) {
        Purchase p = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with ID: " + id));
        return mapToResponse(p);
    }

    private PurchaseResponse mapToResponse(Purchase p) {
        PurchaseResponse resp = new PurchaseResponse();
        resp.setId(p.getId());
        resp.setStoreId(p.getStore().getId());
        resp.setSupplierId(p.getSupplier().getId());
        resp.setSupplierName(p.getSupplier().getName());
        resp.setSupplierPhone(p.getSupplier().getPhone());
        resp.setInvoiceNumber(p.getInvoiceNumber());
        resp.setPurchaseDate(p.getPurchaseDate());
        resp.setSubtotal(p.getSubtotal());
        resp.setTaxAmount(p.getTaxAmount());
        resp.setDiscountAmount(p.getDiscountAmount());
        resp.setTotalAmount(p.getTotalAmount());
        resp.setPaidAmount(p.getPaidAmount());
        resp.setPaymentStatus(p.getPaymentStatus());
        resp.setPaymentMethod(p.getPaymentMethod());
        resp.setNotes(p.getNotes());
        resp.setCreatedByName(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : null);
        resp.setCreatedAt(p.getCreatedAt());

        List<PurchaseResponse.PurchaseItemDto> itemDtos = p.getItems().stream().map(item -> {
            PurchaseResponse.PurchaseItemDto idto = new PurchaseResponse.PurchaseItemDto();
            idto.setId(item.getId());
            idto.setProductId(item.getProduct().getId());
            idto.setProductName(item.getProduct().getName());
            idto.setProductBarcode(item.getProduct().getBarcode());
            idto.setBatchNumber(item.getBatchNumber());
            idto.setExpiryDate(item.getExpiryDate());
            idto.setQuantity(item.getQuantity());
            idto.setCostPrice(item.getCostPrice());
            idto.setGstRate(item.getGstRate());
            idto.setGstAmount(item.getGstAmount());
            idto.setTotalAmount(item.getTotalAmount());
            return idto;
        }).toList();

        resp.setItems(itemDtos);
        return resp;
    }
}
