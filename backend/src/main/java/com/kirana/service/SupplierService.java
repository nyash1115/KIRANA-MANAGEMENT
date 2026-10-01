package com.kirana.service;

import com.kirana.dto.PagedResponse;
import com.kirana.dto.SupplierDto;
import com.kirana.entity.Business;
import com.kirana.entity.Supplier;
import com.kirana.entity.User;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.BusinessRepository;
import com.kirana.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final BusinessRepository businessRepository;
    private final AuditService auditService;

    public SupplierService(SupplierRepository supplierRepository, BusinessRepository businessRepository, AuditService auditService) {
        this.supplierRepository = supplierRepository;
        this.businessRepository = businessRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<SupplierDto> getAllActiveSuppliers() {
        return supplierRepository.findByActiveTrueOrderByNameAsc().stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<SupplierDto> searchSuppliers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Supplier> supplierPage = supplierRepository.searchSuppliers(search, pageable);
        List<SupplierDto> list = supplierPage.getContent().stream().map(this::mapToDto).toList();
        return new PagedResponse<>(list, supplierPage.getNumber(), supplierPage.getSize(), supplierPage.getTotalElements(), supplierPage.getTotalPages(), supplierPage.isLast());
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
        return mapToDto(s);
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto, Long businessId, User user) {
        Business b = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        Supplier s = new Supplier();
        s.setBusiness(b);
        s.setName(dto.getName());
        s.setContactPerson(dto.getContactPerson());
        s.setPhone(dto.getPhone());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());
        s.setGstin(dto.getGstin());
        s.setOutstandingBalance(dto.getOutstandingBalance() != null ? dto.getOutstandingBalance() : BigDecimal.ZERO);
        s.setNotes(dto.getNotes());
        s.setActive(true);

        Supplier saved = supplierRepository.save(s);
        auditService.logAction(b, null, user, "CREATE", "Supplier", String.valueOf(saved.getId()), null, saved.getName(), null);
        return mapToDto(saved);
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto dto, User user) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        s.setName(dto.getName());
        s.setContactPerson(dto.getContactPerson());
        s.setPhone(dto.getPhone());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());
        s.setGstin(dto.getGstin());
        s.setNotes(dto.getNotes());
        s.setActive(dto.isActive());
        s.setUpdatedAt(Instant.now());

        Supplier updated = supplierRepository.save(s);
        auditService.logAction(s.getBusiness(), null, user, "UPDATE", "Supplier", String.valueOf(updated.getId()), null, updated.getName(), null);
        return mapToDto(updated);
    }

    private SupplierDto mapToDto(Supplier s) {
        SupplierDto dto = new SupplierDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setContactPerson(s.getContactPerson());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());
        dto.setGstin(s.getGstin());
        dto.setOutstandingBalance(s.getOutstandingBalance());
        dto.setNotes(s.getNotes());
        dto.setActive(s.isActive());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
