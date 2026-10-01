package com.kirana.service;

import com.kirana.dto.PagedResponse;
import com.kirana.dto.ProductCreateUpdateDto;
import com.kirana.dto.ProductDto;
import com.kirana.entity.*;
import com.kirana.exception.InvalidOperationException;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final BusinessRepository businessRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          UnitRepository unitRepository,
                          BusinessRepository businessRepository,
                          InventoryBatchRepository inventoryBatchRepository,
                          AuditService auditService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.unitRepository = unitRepository;
        this.businessRepository = businessRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> searchProducts(Long storeId, String search, Long categoryId, int page, int size, String sortBy, String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.searchProducts(search, categoryId, pageable);

        // Fetch store stock map for these products
        Map<Long, BigDecimal> stockMap = new HashMap<>();
        if (storeId != null) {
            List<Object[]> stocks = inventoryBatchRepository.getTotalStockPerProduct(storeId);
            for (Object[] row : stocks) {
                stockMap.put((Long) row[0], (BigDecimal) row[1]);
            }
        }

        List<ProductDto> dtoList = productPage.getContent().stream().map(p -> {
            ProductDto dto = mapToDto(p);
            dto.setTotalAvailableStock(stockMap.getOrDefault(p.getId(), BigDecimal.ZERO));
            return dto;
        }).toList();

        return new PagedResponse<>(dtoList, productPage.getNumber(), productPage.getSize(), productPage.getTotalElements(), productPage.getTotalPages(), productPage.isLast());
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id, Long storeId) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        ProductDto dto = mapToDto(p);
        if (storeId != null) {
            List<InventoryBatch> batches = inventoryBatchRepository.findActiveBatchesByProduct(storeId, p.getId());
            BigDecimal totalStock = batches.stream().map(InventoryBatch::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalAvailableStock(totalStock);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public ProductDto getProductByBarcode(String barcode, Long storeId) {
        String cleanCode = barcode != null ? barcode.trim() : "";
        Product p = productRepository.findByBarcode(cleanCode)
                .or(() -> productRepository.findByBarcodeIgnoreCase(cleanCode))
                .or(() -> productRepository.findBySkuIgnoreCase(cleanCode))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode or SKU: " + barcode));
        ProductDto dto = mapToDto(p);
        if (storeId != null) {
            List<InventoryBatch> batches = inventoryBatchRepository.findActiveBatchesByProduct(storeId, p.getId());
            BigDecimal totalStock = batches.stream().map(InventoryBatch::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalAvailableStock(totalStock);
        }
        return dto;
    }

    @Transactional
    public ProductDto createProduct(ProductCreateUpdateDto dto, Long businessId, User currentUser) {
        if (dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty() && productRepository.existsByBarcode(dto.getBarcode())) {
            throw new InvalidOperationException("Product with barcode '" + dto.getBarcode() + "' already exists");
        }
        if (productRepository.existsBySku(dto.getSku())) {
            throw new InvalidOperationException("Product with SKU '" + dto.getSku() + "' already exists");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + dto.getUnitId()));

        Product p = new Product();
        p.setBusiness(business);
        p.setCategory(category);
        p.setUnit(unit);
        p.setName(dto.getName());
        p.setBarcode(dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty() ? dto.getBarcode().trim() : null);
        p.setSku(dto.getSku().trim());
        p.setBrand(dto.getBrand());
        p.setHsnCode(dto.getHsnCode());
        p.setGstRate(dto.getGstRate());
        p.setDefaultCostPrice(dto.getDefaultCostPrice());
        p.setDefaultSellingPrice(dto.getDefaultSellingPrice());
        p.setDefaultMrp(dto.getDefaultMrp());
        if (dto.getMinStockLevel() != null) p.setMinStockLevel(dto.getMinStockLevel());
        if (dto.getReorderLevel() != null) p.setReorderLevel(dto.getReorderLevel());
        p.setImageUrl(dto.getImageUrl());
        p.setDescription(dto.getDescription());
        p.setActive(true);

        Product saved = productRepository.save(p);
        auditService.logAction(business, null, currentUser, "CREATE", "Product", String.valueOf(saved.getId()), null, saved.getName(), null);
        return mapToDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductCreateUpdateDto dto, User currentUser) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty() && !dto.getBarcode().equals(p.getBarcode())) {
            if (productRepository.existsByBarcode(dto.getBarcode())) {
                throw new InvalidOperationException("Product with barcode '" + dto.getBarcode() + "' already exists");
            }
        }
        if (!dto.getSku().equals(p.getSku()) && productRepository.existsBySku(dto.getSku())) {
            throw new InvalidOperationException("Product with SKU '" + dto.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));
        Unit unit = unitRepository.findById(dto.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + dto.getUnitId()));

        String oldVal = "Name=" + p.getName() + ", Price=" + p.getDefaultSellingPrice();

        p.setCategory(category);
        p.setUnit(unit);
        p.setName(dto.getName());
        p.setBarcode(dto.getBarcode() != null && !dto.getBarcode().trim().isEmpty() ? dto.getBarcode().trim() : null);
        p.setSku(dto.getSku().trim());
        p.setBrand(dto.getBrand());
        p.setHsnCode(dto.getHsnCode());
        p.setGstRate(dto.getGstRate());
        p.setDefaultCostPrice(dto.getDefaultCostPrice());
        p.setDefaultSellingPrice(dto.getDefaultSellingPrice());
        p.setDefaultMrp(dto.getDefaultMrp());
        if (dto.getMinStockLevel() != null) p.setMinStockLevel(dto.getMinStockLevel());
        if (dto.getReorderLevel() != null) p.setReorderLevel(dto.getReorderLevel());
        p.setImageUrl(dto.getImageUrl());
        p.setDescription(dto.getDescription());
        p.setUpdatedAt(Instant.now());

        Product updated = productRepository.save(p);
        String newVal = "Name=" + updated.getName() + ", Price=" + updated.getDefaultSellingPrice();
        auditService.logAction(p.getBusiness(), null, currentUser, "UPDATE", "Product", String.valueOf(p.getId()), oldVal, newVal, null);

        return mapToDto(updated);
    }

    @Transactional
    public void deleteProduct(Long id, User currentUser) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        p.setActive(false);
        p.setUpdatedAt(Instant.now());
        productRepository.save(p);
        auditService.logAction(p.getBusiness(), null, currentUser, "DELETE", "Product", String.valueOf(p.getId()), p.getName(), "DEACTIVATED", null);
    }

    private ProductDto mapToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setCategoryId(p.getCategory().getId());
        dto.setCategoryName(p.getCategory().getName());
        dto.setUnitId(p.getUnit().getId());
        dto.setUnitName(p.getUnit().getName());
        dto.setUnitCode(p.getUnit().getCode());
        dto.setAllowDecimals(p.getUnit().isAllowDecimals());
        dto.setName(p.getName());
        dto.setBarcode(p.getBarcode());
        dto.setSku(p.getSku());
        dto.setBrand(p.getBrand());
        dto.setHsnCode(p.getHsnCode());
        dto.setGstRate(p.getGstRate());
        dto.setDefaultCostPrice(p.getDefaultCostPrice());
        dto.setDefaultSellingPrice(p.getDefaultSellingPrice());
        dto.setDefaultMrp(p.getDefaultMrp());
        dto.setMinStockLevel(p.getMinStockLevel());
        dto.setReorderLevel(p.getReorderLevel());
        dto.setImageUrl(p.getImageUrl());
        dto.setDescription(p.getDescription());
        dto.setActive(p.isActive());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
