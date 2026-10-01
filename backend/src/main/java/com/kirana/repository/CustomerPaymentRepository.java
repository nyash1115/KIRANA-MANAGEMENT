package com.kirana.repository;

import com.kirana.entity.CustomerPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    Page<CustomerPayment> findByCustomerIdOrderByPaymentDateDesc(Long customerId, Pageable pageable);
}
