package com.kirana.service;

import com.kirana.dto.AuditLogDto;
import com.kirana.dto.PagedResponse;
import com.kirana.entity.AuditLog;
import com.kirana.entity.Business;
import com.kirana.entity.Store;
import com.kirana.entity.User;
import com.kirana.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(Business business, Store store, User user, String action, String entityName, String entityId, String oldValue, String newValue, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setBusiness(business);
        log.setStore(store);
        log.setUser(user);
        log.setUsername(user != null ? user.getUsername() : "system");
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAuditLogs(Long storeId, String entityName, String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        org.springframework.data.jpa.domain.Specification<AuditLog> spec = com.kirana.specification.AuditLogSpecification.withFilters(storeId, entityName, action);
        Page<AuditLog> logPage = auditLogRepository.findAll(spec, pageable);

        var list = logPage.getContent().stream().map(log -> {
            AuditLogDto dto = new AuditLogDto();
            dto.setId(log.getId());
            dto.setBusinessId(log.getBusiness() != null ? log.getBusiness().getId() : null);
            dto.setStoreId(log.getStore() != null ? log.getStore().getId() : null);
            dto.setUserId(log.getUser() != null ? log.getUser().getId() : null);
            dto.setUsername(log.getUsername());
            dto.setAction(log.getAction());
            dto.setEntityName(log.getEntityName());
            dto.setEntityId(log.getEntityId());
            dto.setOldValue(log.getOldValue());
            dto.setNewValue(log.getNewValue());
            dto.setIpAddress(log.getIpAddress());
            dto.setCreatedAt(log.getCreatedAt());
            return dto;
        }).toList();

        return new PagedResponse<>(list, logPage.getNumber(), logPage.getSize(), logPage.getTotalElements(), logPage.getTotalPages(), logPage.isLast());
    }
}
