package com.kirana.repository;

import com.kirana.entity.KhataTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhataTransactionRepository extends JpaRepository<KhataTransaction, Long> {
    List<KhataTransaction> findByCustomerIdOrderByTransactionDateDesc(Long customerId);

    @Query("SELECT k FROM KhataTransaction k WHERE k.customer.id = :customerId ORDER BY k.transactionDate DESC, k.id DESC")
    Page<KhataTransaction> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
}
