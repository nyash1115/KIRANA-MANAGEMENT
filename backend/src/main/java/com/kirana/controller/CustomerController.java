package com.kirana.controller;

import com.kirana.dto.*;
import com.kirana.entity.User;
import com.kirana.repository.UserRepository;
import com.kirana.security.UserPrincipal;
import com.kirana.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers & Khata", description = "Endpoints for managing customer profiles, credit limits, and Khata (Udhaar) ledgers")
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;

    public CustomerController(CustomerService customerService, UserRepository userRepository) {
        this.customerService = customerService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Search customers by name or phone with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerDto>>> searchCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(customerService.searchCustomers(search, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id)));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Quick POS customer search by 10-digit mobile number")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerByPhone(phone)));
    }

    @PostMapping
    @Operation(summary = "Create a new customer profile")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CustomerDto dto) {
        Long businessId = principal != null && principal.getBusinessId() != null ? principal.getBusinessId() : 1L;
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        CustomerDto created = customerService.createCustomer(dto, businessId, user);
        return ResponseEntity.ok(ApiResponse.success("Customer created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CustomerDto dto) {
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        CustomerDto updated = customerService.updateCustomer(id, dto, user);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }

    @GetMapping("/{id}/khata")
    @Operation(summary = "Get customer Khata (credit) statement with running balances")
    public ResponseEntity<ApiResponse<PagedResponse<KhataTransactionDto>>> getCustomerKhata(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerKhata(id, page, size)));
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Record customer payment to reduce outstanding Udhaar")
    public ResponseEntity<ApiResponse<KhataTransactionDto>> recordPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CustomerPaymentRequest request) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        KhataTransactionDto tx = customerService.recordPayment(storeId, id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded and customer balance updated", tx));
    }
}
