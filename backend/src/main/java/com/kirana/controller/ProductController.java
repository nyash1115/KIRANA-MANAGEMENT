package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.PagedResponse;
import com.kirana.dto.ProductCreateUpdateDto;
import com.kirana.dto.ProductDto;
import com.kirana.entity.User;
import com.kirana.repository.UserRepository;
import com.kirana.security.UserPrincipal;
import com.kirana.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Catalog", description = "Endpoints for managing products, barcodes, and prices")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Search products with filters, sorting, and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> searchProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Long storeId = principal != null ? principal.getStoreId() : 1L;
        PagedResponse<ProductDto> result = productService.searchProducts(storeId, search, categoryId, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id, storeId)));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Fast barcode scan lookup for POS checkout")
    public ResponseEntity<ApiResponse<ProductDto>> getProductByBarcode(@PathVariable String barcode, @AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(productService.getProductByBarcode(barcode, storeId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid @RequestBody ProductCreateUpdateDto dto) {
        Long businessId = principal != null && principal.getBusinessId() != null ? principal.getBusinessId() : 1L;
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        ProductDto created = productService.createProduct(dto, businessId, user);
        return ResponseEntity.ok(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid @RequestBody ProductCreateUpdateDto dto) {
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        ProductDto updated = productService.updateProduct(id, dto, user);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        User user = principal != null ? userRepository.findById(principal.getId()).orElse(null) : null;
        productService.deleteProduct(id, user);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
