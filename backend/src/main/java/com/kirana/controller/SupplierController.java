package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.PagedResponse;
import com.kirana.dto.SupplierDto;
import com.kirana.entity.User;
import com.kirana.repository.UserRepository;
import com.kirana.security.UserPrincipal;
import com.kirana.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Suppliers", description = "Endpoints for managing vendors, suppliers, and procurement balances")
public class SupplierController {

    private final SupplierService supplierService;
    private final UserRepository userRepository;

    public SupplierController(SupplierService supplierService, UserRepository userRepository) {
        this.supplierService = supplierService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Search suppliers with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<SupplierDto>>> searchSuppliers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.searchSuppliers(search, page, size)));
    }

    @GetMapping("/active")
    @Operation(summary = "Get list of all active suppliers for purchase dropdowns")
    public ResponseEntity<ApiResponse<List<SupplierDto>>> getAllActiveSuppliers() {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAllActiveSuppliers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<ApiResponse<SupplierDto>> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplierById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<ApiResponse<SupplierDto>> createSupplier(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SupplierDto dto) {
        Long businessId = principal != null && principal.getBusinessId() != null ? principal.getBusinessId() : 1L;
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        SupplierDto created = supplierService.createSupplier(dto, businessId, user);
        return ResponseEntity.ok(ApiResponse.success("Supplier created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<ApiResponse<SupplierDto>> updateSupplier(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SupplierDto dto) {
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        SupplierDto updated = supplierService.updateSupplier(id, dto, user);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", updated));
    }
}
