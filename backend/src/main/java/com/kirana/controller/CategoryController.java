package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.CategoryDto;
import com.kirana.dto.UnitDto;
import com.kirana.security.UserPrincipal;
import com.kirana.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Categories & Units", description = "Endpoints for managing product categories and units of measurement")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    @Operation(summary = "List all active product categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new product category")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @Valid @RequestBody CategoryDto dto) {
        Long businessId = principal.getBusinessId() != null ? principal.getBusinessId() : 1L;
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", categoryService.createCategory(dto, businessId)));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", categoryService.updateCategory(id, dto)));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    @GetMapping("/units")
    @Operation(summary = "List all units of measurement")
    public ResponseEntity<ApiResponse<List<UnitDto>>> getUnits() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllUnits()));
    }
}
