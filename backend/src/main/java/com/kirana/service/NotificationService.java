package com.kirana.service;

import com.kirana.dto.NotificationDto;
import com.kirana.dto.PagedResponse;
import com.kirana.entity.*;
import com.kirana.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               ProductRepository productRepository,
                               InventoryBatchRepository inventoryBatchRepository,
                               CustomerRepository customerRepository,
                               StoreRepository storeRepository) {
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public void syncAlerts(Long storeId) {
        if (storeId == null) storeId = 1L;
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) return;

        // 1. Check near expiry (next 30 days)
        LocalDate today = LocalDate.now();
        List<InventoryBatch> expiring = inventoryBatchRepository.findExpiringBatches(storeId, today, today.plusDays(30));
        for (InventoryBatch b : expiring) {
            if (!notificationRepository.existsByStoreIdAndTypeAndReferenceId(storeId, "NEAR_EXPIRY", String.valueOf(b.getId()))) {
                createNotification(store, "NEAR_EXPIRY", "Batch Expiring Soon: " + b.getProduct().getName(),
                        "Batch " + b.getBatchNumber() + " (" + b.getQuantity() + " " + b.getProduct().getUnit().getCode() + ") expires on " + b.getExpiryDate(),
                        "WARNING", "InventoryBatch", String.valueOf(b.getId()));
            }
        }

        // 2. Check low stock products
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            List<InventoryBatch> batches = inventoryBatchRepository.findActiveBatchesByProduct(storeId, p.getId());
            BigDecimal totalStock = batches.stream().map(InventoryBatch::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal reorderLevel = p.getReorderLevel() != null ? p.getReorderLevel() : BigDecimal.TEN;
            if (totalStock.compareTo(reorderLevel) <= 0) {
                String type = totalStock.compareTo(BigDecimal.ZERO) <= 0 ? "OUT_OF_STOCK" : "LOW_STOCK";
                String severity = totalStock.compareTo(BigDecimal.ZERO) <= 0 ? "CRITICAL" : "WARNING";
                if (!notificationRepository.existsByStoreIdAndTypeAndReferenceId(storeId, type, String.valueOf(p.getId()))) {
                    createNotification(store, type, (type.equals("OUT_OF_STOCK") ? "Out of Stock: " : "Low Stock Alert: ") + p.getName(),
                            "Current available stock is " + totalStock + " " + p.getUnit().getCode() + " (Reorder level: " + p.getReorderLevel() + ")",
                            severity, "Product", String.valueOf(p.getId()));
                }
            }
        }

        // 3. Check customer khata balances
        List<Customer> customers = customerRepository.findAll();
        for (Customer c : customers) {
            if (c.getCurrentOutstanding() != null && c.getCurrentOutstanding().compareTo(BigDecimal.ZERO) > 0) {
                if (!notificationRepository.existsByStoreIdAndTypeAndReferenceId(storeId, "CUSTOMER_PAYMENT_DUE", String.valueOf(c.getId()))) {
                    createNotification(store, "CUSTOMER_PAYMENT_DUE", "Khata Balance Due: " + c.getName(),
                            c.getName() + " has an outstanding credit balance of Rs " + c.getCurrentOutstanding() + ". Phone: " + c.getPhone(),
                            "INFO", "Customer", String.valueOf(c.getId()));
                }
            }
        }
    }

    @Transactional
    public void createNotification(Store store, String type, String title, String message, String severity, String referenceType, String referenceId) {
        Notification notification = new Notification();
        notification.setStore(store);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSeverity(severity != null ? severity : "INFO");
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);
    }

    @Transactional
    public List<NotificationDto> getUnreadNotifications(Long storeId) {
        syncAlerts(storeId);
        return notificationRepository.findByStoreIdAndReadFalseOrderByCreatedAtDesc(storeId)
                .stream().map(this::mapToDto).toList();
    }

    @Transactional
    public PagedResponse<NotificationDto> getAllNotifications(Long storeId, int page, int size) {
        syncAlerts(storeId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifPage = notificationRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable);
        List<NotificationDto> dtoList = notifPage.getContent().stream().map(this::mapToDto).toList();
        return new PagedResponse<>(dtoList, notifPage.getNumber(), notifPage.getSize(), notifPage.getTotalElements(), notifPage.getTotalPages(), notifPage.isLast());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            n.setReadAt(Instant.now());
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long storeId) {
        notificationRepository.markAllAsRead(storeId);
    }

    private NotificationDto mapToDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setStoreId(n.getStore().getId());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setSeverity(n.getSeverity());
        dto.setReferenceType(n.getReferenceType());
        dto.setReferenceId(n.getReferenceId());
        dto.setRead(n.isRead());
        dto.setReadAt(n.getReadAt());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
