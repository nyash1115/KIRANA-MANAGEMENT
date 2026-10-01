package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.InventoryBatchDto;
import com.kirana.dto.PagedResponse;
import com.kirana.dto.ProductDto;
import com.kirana.dto.StockAdjustmentRequest;
import com.kirana.entity.User;
import com.kirana.repository.UserRepository;
import com.kirana.security.UserPrincipal;
import com.kirana.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory & Batches", description = "Endpoints for FEFO batch tracking, low-stock alerts, expiry monitoring, and stock adjustments")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    public InventoryController(InventoryService inventoryService, UserRepository userRepository) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/batches")
    @Operation(summary = "Search inventory batches with status, search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryBatchDto>>> searchBatches(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(inventoryService.searchBatches(storeId, search, status, page, size)));
    }

    @GetMapping("/fefo/{productId}")
    @Operation(summary = "Get valid batches for a product in FEFO (First-Expiry, First-Out) priority")
    public ResponseEntity<ApiResponse<List<InventoryBatchDto>>> getFefoBatches(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getFefoBatches(storeId, productId)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products running at or below reorder threshold")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getLowStockProducts(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockProducts(storeId)));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get batches expiring within configured warning days")
    public ResponseEntity<ApiResponse<List<InventoryBatchDto>>> getExpiringBatches(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "30") int days) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getExpiringBatches(storeId, days)));
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired inventory batches")
    public ResponseEntity<ApiResponse<List<InventoryBatchDto>>> getExpiredBatches(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getExpiredBatches(storeId)));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Manually adjust stock for damages, expiration, or physical count corrections")
    public ResponseEntity<ApiResponse<InventoryBatchDto>> adjustStock(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StockAdjustmentRequest request) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        InventoryBatchDto dto = inventoryService.adjustStock(storeId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", dto));
    }
}
