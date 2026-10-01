package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.StoreSettingsDto;
import com.kirana.security.UserPrincipal;
import com.kirana.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Store Settings", description = "Endpoints for configuring branch parameters, tax modes, and invoice formatting")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    @Operation(summary = "Get settings for current store")
    public ResponseEntity<ApiResponse<StoreSettingsDto>> getStoreSettings(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(settingsService.getStoreSettings(storeId)));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update store settings (Admin only)")
    public ResponseEntity<ApiResponse<StoreSettingsDto>> updateStoreSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody StoreSettingsDto dto) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success("Store settings updated successfully", settingsService.updateStoreSettings(storeId, dto)));
    }
}
