package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.NotificationDto;
import com.kirana.dto.PagedResponse;
import com.kirana.security.UserPrincipal;
import com.kirana.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications & Alerts", description = "Endpoints for managing store alerts (low stock, expiry, payment dues)")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread operational notifications")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnreadNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadNotifications(storeId)));
    }

    @GetMapping
    @Operation(summary = "Get all notifications with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationDto>>> getAllNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAllNotifications(storeId, page, size)));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PostMapping("/sync")
    @Operation(summary = "Manually trigger background alert scan (low stock, near expiry, customer dues)")
    public ResponseEntity<ApiResponse<Void>> syncAlerts(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        notificationService.syncAlerts(storeId);
        return ResponseEntity.ok(ApiResponse.success("Alerts synced successfully", null));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        Long storeId = principal != null && principal.getStoreId() != null ? principal.getStoreId() : 1L;
        notificationService.markAllAsRead(storeId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
