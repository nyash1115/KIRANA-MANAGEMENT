package com.kirana.repository;

import com.kirana.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {
    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT s FROM Sale s WHERE s.store.id = :storeId " +
           "AND (:invoiceNumber IS NULL OR :invoiceNumber = '' OR LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%'))) " +
           "AND (:customerId IS NULL OR s.customer.id = :customerId) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:startDate IS NULL OR s.saleDate >= :startDate) " +
           "AND (:endDate IS NULL OR s.saleDate <= :endDate) " +
           "ORDER BY s.saleDate DESC")
    Page<Sale> findSalesWithFilters(@Param("storeId") Long storeId,
                                   @Param("invoiceNumber") String invoiceNumber,
                                   @Param("customerId") Long customerId,
                                   @Param("status") String status,
                                   @Param("startDate") Instant startDate,
                                   @Param("endDate") Instant endDate,
                                   Pageable pageable);

    @Query("SELECT COUNT(s), COALESCE(SUM(s.totalAmount), 0), COALESCE(SUM(s.grossProfit), 0), COALESCE(SUM(s.totalCogs), 0) " +
           "FROM Sale s WHERE s.store.id = :storeId AND s.status = 'COMPLETED' " +
           "AND s.saleDate >= :startDate AND s.saleDate <= :endDate")
    List<Object[]> getSalesAggregateMetrics(@Param("storeId") Long storeId,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate);

    @Query("SELECT FUNCTION('DATE', s.saleDate), COUNT(s), SUM(s.totalAmount), SUM(s.grossProfit) " +
           "FROM Sale s WHERE s.store.id = :storeId AND s.status = 'COMPLETED' " +
           "AND s.saleDate >= :startDate AND s.saleDate <= :endDate " +
           "GROUP BY FUNCTION('DATE', s.saleDate) " +
           "ORDER BY FUNCTION('DATE', s.saleDate) ASC")
    List<Object[]> getDailySalesTrends(@Param("storeId") Long storeId,
                                       @Param("startDate") Instant startDate,
                                       @Param("endDate") Instant endDate);
}
