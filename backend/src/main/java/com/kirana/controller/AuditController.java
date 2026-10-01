package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.AuditLogDto;
import com.kirana.dto.PagedResponse;
import com.kirana.security.UserPrincipal;
import com.kirana.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/audit", "/api/v1/audit-logs"})
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "Endpoints for viewing immutable system action logs (Admin only)")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Search audit log records with filters (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getAuditLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long storeId = principal != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(auditService.getAuditLogs(storeId, entityName, action, page, size)));
    }
}
