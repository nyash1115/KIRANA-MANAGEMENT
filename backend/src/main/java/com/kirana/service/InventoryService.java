package com.kirana.service;

import com.kirana.dto.InventoryBatchDto;
import com.kirana.dto.PagedResponse;
import com.kirana.dto.ProductDto;
import com.kirana.dto.StockAdjustmentRequest;
import com.kirana.entity.InventoryBatch;
import com.kirana.entity.Product;
import com.kirana.entity.StockAdjustment;
import com.kirana.entity.Store;
import com.kirana.entity.User;
import com.kirana.exception.InvalidOperationException;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.InventoryBatchRepository;
import com.kirana.repository.ProductRepository;
import com.kirana.repository.StockAdjustmentRepository;
import com.kirana.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public InventoryService(InventoryBatchRepository inventoryBatchRepository,
                            ProductRepository productRepository,
                            StoreRepository storeRepository,
                            StockAdjustmentRepository stockAdjustmentRepository,
                            AuditService auditService,
                            NotificationService notificationService) {
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryBatchDto> searchBatches(Long storeId, String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<InventoryBatch> batchPage = inventoryBatchRepository.searchBatches(storeId, search, status, pageable);
        List<InventoryBatchDto> list = batchPage.getContent().stream().map(this::mapToDto).toList();
        return new PagedResponse<>(list, batchPage.getNumber(), batchPage.getSize(), batchPage.getTotalElements(), batchPage.getTotalPages(), batchPage.isLast());
    }

    @Transactional(readOnly = true)
    public List<InventoryBatchDto> getFefoBatches(Long storeId, Long productId) {
        return inventoryBatchRepository.findFefoBatches(storeId, productId, LocalDate.now())
                .stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getLowStockProducts(Long storeId) {
        List<Object[]> stocks = inventoryBatchRepository.getTotalStockPerProduct(storeId);
        Map<Long, BigDecimal> stockMap = new HashMap<>();
        for (Object[] row : stocks) {
            stockMap.put((Long) row[0], (BigDecimal) row[1]);
        }

        List<Product> allProducts = productRepository.findAll();
        List<ProductDto> lowStock = new ArrayList<>();

        for (Product p : allProducts) {
            if (!p.isActive()) continue;
            BigDecimal current = stockMap.getOrDefault(p.getId(), BigDecimal.ZERO);
            if (current.compareTo(p.getMinStockLevel()) <= 0) {
                ProductDto dto = new ProductDto();
                dto.setId(p.getId());
                dto.setName(p.getName());
                dto.setBarcode(p.getBarcode());
                dto.setSku(p.getSku());
                dto.setUnitName(p.getUnit().getName());
                dto.setMinStockLevel(p.getMinStockLevel());
                dto.setReorderLevel(p.getReorderLevel());
                dto.setTotalAvailableStock(current);
                lowStock.add(dto);
            }
        }
        return lowStock;
    }

    @Transactional(readOnly = true)
    public List<InventoryBatchDto> getExpiringBatches(Long storeId, int warningDays) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(warningDays);
        return inventoryBatchRepository.findExpiringBatches(storeId, today, threshold)
                .stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryBatchDto> getExpiredBatches(Long storeId) {
        return inventoryBatchRepository.findExpiredBatches(storeId, LocalDate.now())
                .stream().map(this::mapToDto).toList();
    }

    @Transactional
    public InventoryBatchDto adjustStock(Long storeId, StockAdjustmentRequest request, User user) {
        InventoryBatch batch = inventoryBatchRepository.findByIdWithLock(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + request.getBatchId()));

        if (!batch.getStore().getId().equals(storeId)) {
            throw new InvalidOperationException("Batch does not belong to the selected store");
        }

        BigDecimal before = batch.getQuantity();
        BigDecimal adj = request.getAdjustedQuantity();
        BigDecimal after = before.add(adj);

        if (after.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Cannot adjust quantity below zero. Available: " + before + ", Adjustment: " + adj);
        }

        batch.setQuantity(after);
        if (after.compareTo(BigDecimal.ZERO) == 0) {
            batch.setStatus("DEPLETED");
        } else {
            batch.setStatus("ACTIVE");
        }
        inventoryBatchRepository.save(batch);

        BigDecimal costImpact = adj.multiply(batch.getCostPrice());

        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setStore(batch.getStore());
        adjustment.setProduct(batch.getProduct());
        adjustment.setInventoryBatch(batch);
        adjustment.setAdjustmentType(request.getAdjustmentType());
        adjustment.setQuantityBefore(before);
        adjustment.setAdjustedQuantity(adj);
        adjustment.setQuantityAfter(after);
        adjustment.setCostImpact(costImpact);
        adjustment.setReason(request.getReason());
        adjustment.setAdjustedBy(user);
        stockAdjustmentRepository.save(adjustment);

        auditService.logAction(batch.getProduct().getBusiness(), batch.getStore(), user, "STOCK_ADJUSTMENT",
                "InventoryBatch", String.valueOf(batch.getId()), "Qty: " + before, "Qty: " + after + " (" + request.getAdjustmentType() + ")", null);

        return mapToDto(batch);
    }

    private InventoryBatchDto mapToDto(InventoryBatch b) {
        InventoryBatchDto dto = new InventoryBatchDto();
        dto.setId(b.getId());
        dto.setStoreId(b.getStore().getId());
        dto.setProductId(b.getProduct().getId());
        dto.setProductName(b.getProduct().getName());
        dto.setProductBarcode(b.getProduct().getBarcode());
        dto.setProductSku(b.getProduct().getSku());
        dto.setUnitName(b.getProduct().getUnit().getName());
        dto.setUnitCode(b.getProduct().getUnit().getCode());
        dto.setBatchNumber(b.getBatchNumber());
        dto.setQuantity(b.getQuantity());
        dto.setInitialQuantity(b.getInitialQuantity());
        dto.setCostPrice(b.getCostPrice());
        dto.setSellingPrice(b.getSellingPrice());
        dto.setMrp(b.getMrp());
        dto.setExpiryDate(b.getExpiryDate());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());

        LocalDate today = LocalDate.now();
        if (b.getExpiryDate() != null) {
            dto.setExpired(b.getExpiryDate().isBefore(today));
            dto.setNearExpiry(!dto.isExpired() && b.getExpiryDate().isBefore(today.plusDays(30)));
        }
        return dto;
    }
}
