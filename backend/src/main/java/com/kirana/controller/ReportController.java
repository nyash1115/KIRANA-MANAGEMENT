package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.security.UserPrincipal;
import com.kirana.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports & Analytics", description = "Endpoints for financial statements, P&L, GST summaries, and inventory valuation")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    private Instant parseDate(String dateStr, boolean isEnd) {
        if (dateStr == null || dateStr.isBlank()) {
            return isEnd ? Instant.now() : Instant.now().minus(30, ChronoUnit.DAYS);
        }
        try {
            if (dateStr.length() == 10) {
                LocalDate ld = LocalDate.parse(dateStr);
                return isEnd ? ld.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()
                             : ld.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
            }
            return Instant.parse(dateStr);
        } catch (Exception e) {
            return isEnd ? Instant.now() : Instant.now().minus(30, ChronoUnit.DAYS);
        }
    }

    @GetMapping("/profit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Gross Profit & Loss summary (Net Sales, COGS, Gross Profit, Margin %)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfitReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        Instant start = parseDate(startDate, false);
        Instant end = parseDate(endDate, true);
        return ResponseEntity.ok(ApiResponse.success(reportService.getProfitReport(storeId, start, end)));
    }

    @GetMapping("/gst-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get GST compliance summary broken down by tax slabs (0%, 5%, 12%, 18%, 28%)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGstSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        Instant start = parseDate(startDate, false);
        Instant end = parseDate(endDate, true);
        return ResponseEntity.ok(ApiResponse.success(reportService.getGstSummary(storeId, start, end)));
    }

    @GetMapping("/inventory-valuation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get total inventory valuation at cost basis and expected retail turnover")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventoryValuation(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(reportService.getInventoryValuation(storeId)));
    }

    @GetMapping(value = "/sales/export", produces = "text/csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export sales report to CSV")
    public ResponseEntity<String> exportSalesCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        if (endDate == null) endDate = Instant.now();
        if (startDate == null) startDate = endDate.minus(30, ChronoUnit.DAYS);

        String csv = reportService.exportSalesCsv(storeId, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report-" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
