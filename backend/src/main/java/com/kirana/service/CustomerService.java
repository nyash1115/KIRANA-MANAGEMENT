package com.kirana.service;

import com.kirana.dto.CustomerDto;
import com.kirana.dto.CustomerPaymentRequest;
import com.kirana.dto.KhataTransactionDto;
import com.kirana.dto.PagedResponse;
import com.kirana.entity.Business;
import com.kirana.entity.Customer;
import com.kirana.entity.CustomerPayment;
import com.kirana.entity.KhataTransaction;
import com.kirana.entity.Store;
import com.kirana.entity.User;
import com.kirana.exception.InvalidOperationException;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.BusinessRepository;
import com.kirana.repository.CustomerPaymentRepository;
import com.kirana.repository.CustomerRepository;
import com.kirana.repository.KhataTransactionRepository;
import com.kirana.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KhataTransactionRepository khataTransactionRepository;
    private final CustomerPaymentRepository customerPaymentRepository;
    private final BusinessRepository businessRepository;
    private final StoreRepository storeRepository;
    private final AuditService auditService;

    public CustomerService(CustomerRepository customerRepository,
                           KhataTransactionRepository khataTransactionRepository,
                           CustomerPaymentRepository customerPaymentRepository,
                           BusinessRepository businessRepository,
                           StoreRepository storeRepository,
                           AuditService auditService) {
        this.customerRepository = customerRepository;
        this.khataTransactionRepository = khataTransactionRepository;
        this.customerPaymentRepository = customerPaymentRepository;
        this.businessRepository = businessRepository;
        this.storeRepository = storeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerDto> searchCustomers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> custPage = customerRepository.searchCustomers(search, pageable);
        List<CustomerDto> list = custPage.getContent().stream().map(this::mapToDto).toList();
        return new PagedResponse<>(list, custPage.getNumber(), custPage.getSize(), custPage.getTotalElements(), custPage.getTotalPages(), custPage.isLast());
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        return mapToDto(c);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPhone(String phone) {
        Customer c = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));
        return mapToDto(c);
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto, Long businessId, User user) {
        Business b = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        Customer c = new Customer();
        c.setBusiness(b);
        c.setName(dto.getName());
        c.setPhone(dto.getPhone());
        c.setEmail(dto.getEmail());
        c.setAddress(dto.getAddress());
        c.setGstin(dto.getGstin());
        c.setCreditLimit(dto.getCreditLimit() != null ? dto.getCreditLimit() : new BigDecimal("5000.00"));
        c.setCurrentOutstanding(BigDecimal.ZERO);
        c.setNotes(dto.getNotes());
        c.setActive(true);

        Customer saved = customerRepository.save(c);
        auditService.logAction(b, null, user, "CREATE", "Customer", String.valueOf(saved.getId()), null, saved.getName(), null);
        return mapToDto(saved);
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto dto, User user) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        c.setName(dto.getName());
        c.setPhone(dto.getPhone());
        c.setEmail(dto.getEmail());
        c.setAddress(dto.getAddress());
        c.setGstin(dto.getGstin());
        if (dto.getCreditLimit() != null) c.setCreditLimit(dto.getCreditLimit());
        c.setNotes(dto.getNotes());
        c.setActive(dto.isActive());
        c.setUpdatedAt(Instant.now());

        Customer updated = customerRepository.save(c);
        auditService.logAction(c.getBusiness(), null, user, "UPDATE", "Customer", String.valueOf(updated.getId()), null, updated.getName(), null);
        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public PagedResponse<KhataTransactionDto> getCustomerKhata(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<KhataTransaction> ktPage = khataTransactionRepository.findByCustomerId(customerId, pageable);
        List<KhataTransactionDto> list = ktPage.getContent().stream().map(this::mapKhataToDto).toList();
        return new PagedResponse<>(list, ktPage.getNumber(), ktPage.getSize(), ktPage.getTotalElements(), ktPage.getTotalPages(), ktPage.isLast());
    }

    @Transactional
    public KhataTransactionDto recordPayment(Long storeId, Long customerId, CustomerPaymentRequest request, User currentUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        if (customer.getCurrentOutstanding().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Customer has no outstanding credit balance");
        }

        BigDecimal payAmount = request.getAmount();
        BigDecimal newBalance = customer.getCurrentOutstanding().subtract(payAmount);
        customer.setCurrentOutstanding(newBalance);
        customerRepository.save(customer);

        String paymentNo = "PAY-" + System.currentTimeMillis();

        CustomerPayment payment = new CustomerPayment();
        payment.setStore(store);
        payment.setCustomer(customer);
        payment.setPaymentNumber(paymentNo);
        payment.setAmount(payAmount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setNotes(request.getNotes());
        payment.setCreatedBy(currentUser);
        customerPaymentRepository.save(payment);

        KhataTransaction kt = new KhataTransaction();
        kt.setStore(store);
        kt.setCustomer(customer);
        kt.setType("CREDIT_PAYMENT");
        kt.setReferenceType("CUSTOMER_PAYMENT");
        kt.setReferenceId(paymentNo);
        kt.setAmount(payAmount);
        kt.setBalanceAfter(newBalance);
        kt.setNotes(request.getNotes() != null ? request.getNotes() : ("Udhaar Repayment via " + request.getPaymentMethod()));
        kt.setCreatedBy(currentUser);
        KhataTransaction savedKt = khataTransactionRepository.save(kt);

        auditService.logAction(store.getBusiness(), store, currentUser, "KHATA_PAYMENT", "Customer", String.valueOf(customer.getId()),
                "Outstanding Before: " + (newBalance.add(payAmount)), "Paid: " + payAmount + ", New Outstanding: " + newBalance, null);

        return mapKhataToDto(savedKt);
    }

    private CustomerDto mapToDto(Customer c) {
        CustomerDto dto = new CustomerDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setPhone(c.getPhone());
        dto.setEmail(c.getEmail());
        dto.setAddress(c.getAddress());
        dto.setGstin(c.getGstin());
        dto.setCreditLimit(c.getCreditLimit());
        dto.setCurrentOutstanding(c.getCurrentOutstanding());
        dto.setNotes(c.getNotes());
        dto.setActive(c.isActive());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    private KhataTransactionDto mapKhataToDto(KhataTransaction kt) {
        KhataTransactionDto dto = new KhataTransactionDto();
        dto.setId(kt.getId());
        dto.setCustomerId(kt.getCustomer().getId());
        dto.setCustomerName(kt.getCustomer().getName());
        dto.setCustomerPhone(kt.getCustomer().getPhone());
        dto.setType(kt.getType());
        dto.setReferenceType(kt.getReferenceType());
        dto.setReferenceId(kt.getReferenceId());
        dto.setAmount(kt.getAmount());
        dto.setBalanceAfter(kt.getBalanceAfter());
        dto.setNotes(kt.getNotes());
        dto.setCreatedByName(kt.getCreatedBy() != null ? kt.getCreatedBy().getFullName() : null);
        dto.setTransactionDate(kt.getTransactionDate());
        return dto;
    }
}
