package com.kirana.controller;

import com.kirana.dto.*;
import com.kirana.entity.User;
import com.kirana.repository.UserRepository;
import com.kirana.security.UserPrincipal;
import com.kirana.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "POS & Sales Billing", description = "Endpoints for cart calculations, checkout, invoice generation, cancellations, and returns")
public class SaleController {

    private final SaleService saleService;
    private final UserRepository userRepository;

    public SaleController(SaleService saleService, UserRepository userRepository) {
        this.saleService = saleService;
        this.userRepository = userRepository;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Real-time cart calculation (GST, item discounts, bill discounts, roundoff)")
    public ResponseEntity<ApiResponse<BillCalculationResponse>> calculateBill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BillCalculationRequest request) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(saleService.calculateBill(storeId, request)));
    }

    @PostMapping
    @Operation(summary = "Execute POS checkout (deducts FEFO stock, generates bill, records split payments & Khata)")
    public ResponseEntity<ApiResponse<SaleResponse>> checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckoutRequest request) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        User cashier = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        SaleResponse response = saleService.checkout(storeId, request, cashier);
        return ResponseEntity.ok(ApiResponse.success("Sale completed successfully", response));
    }

    @GetMapping
    @Operation(summary = "Query sales invoices with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<SaleResponse>>> getSales(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(saleService.getSales(storeId, invoiceNumber, customerId, status, startDate, endDate, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed sale invoice (with line items and payment breakdown)")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleById(id)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cancel a bill (restocks inventory and reverses Khata credit)")
    public ResponseEntity<ApiResponse<SaleResponse>> cancelSale(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        String reason = body.getOrDefault("reason", "Cancelled by authorized staff");
        SaleResponse cancelled = saleService.cancelSale(id, reason, user);
        return ResponseEntity.ok(ApiResponse.success("Bill cancelled and inventory reversed", cancelled));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Process sales return for customer (restocks returned batches and issues refund/credit)")
    public ResponseEntity<ApiResponse<SaleResponse>> processReturn(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SaleReturnRequest request) {
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        SaleResponse ret = saleService.processReturn(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Return processed and inventory restocked", ret));
    }
}
