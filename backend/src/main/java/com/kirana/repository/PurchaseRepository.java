package com.kirana.repository;

import com.kirana.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("SELECT p FROM Purchase p WHERE p.store.id = :storeId " +
           "AND (:supplierId IS NULL OR p.supplier.id = :supplierId) " +
           "AND (:startDate IS NULL OR p.purchaseDate >= :startDate) " +
           "AND (:endDate IS NULL OR p.purchaseDate <= :endDate) " +
           "ORDER BY p.purchaseDate DESC, p.id DESC")
    Page<Purchase> findPurchasesWithFilters(@Param("storeId") Long storeId,
                                          @Param("supplierId") Long supplierId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);
}
