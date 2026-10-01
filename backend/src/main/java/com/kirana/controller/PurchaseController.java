package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.PagedResponse;
import com.kirana.dto.PurchaseRequest;
import com.kirana.dto.PurchaseResponse;
import com.kirana.entity.User;
import com.kirana.repository.UserRepository;
import com.kirana.security.UserPrincipal;
import com.kirana.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/purchases")
@Tag(name = "Purchases", description = "Endpoints for inward inventory purchases, supplier invoices, and stock replenishment")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserRepository userRepository;

    public PurchaseController(PurchaseService purchaseService, UserRepository userRepository) {
        this.purchaseService = purchaseService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "List purchase invoices with supplier and date filters")
    public ResponseEntity<ApiResponse<PagedResponse<PurchaseResponse>>> getPurchases(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(purchaseService.getPurchases(storeId, supplierId, startDate, endDate, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase invoice details with line items")
    public ResponseEntity<ApiResponse<PurchaseResponse>> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseService.getPurchaseById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create an inward purchase (automatically creates/increments inventory batches)")
    public ResponseEntity<ApiResponse<PurchaseResponse>> createPurchase(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PurchaseRequest request) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        PurchaseResponse response = purchaseService.createPurchase(storeId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Purchase recorded and stock added successfully", response));
    }
}
