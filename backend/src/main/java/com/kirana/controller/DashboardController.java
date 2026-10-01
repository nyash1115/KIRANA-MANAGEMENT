package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.DashboardChartsDto;
import com.kirana.dto.DashboardSummaryDto;
import com.kirana.security.UserPrincipal;
import com.kirana.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for store KPIs, real-time counters, and charts")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get daily KPI metrics (today sales, profit, stock valuations, dues)")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary(storeId)));
    }

    @GetMapping("/charts")
    @Operation(summary = "Get chart analytics (sales trend, top products, category split)")
    public ResponseEntity<ApiResponse<DashboardChartsDto>> getCharts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "week") String period) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getCharts(storeId, period)));
    }
}
