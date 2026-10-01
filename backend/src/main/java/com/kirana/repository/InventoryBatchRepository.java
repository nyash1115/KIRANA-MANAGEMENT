package com.kirana.repository;

import com.kirana.entity.InventoryBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM InventoryBatch b WHERE b.id = :id")
    Optional<InventoryBatch> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT b FROM InventoryBatch b WHERE b.store.id = :storeId " +
           "AND b.product.id = :productId AND b.quantity > 0 " +
           "AND (b.expiryDate IS NULL OR b.expiryDate >= :today) " +
           "ORDER BY b.expiryDate ASC NULLS LAST, b.id ASC")
    List<InventoryBatch> findFefoBatches(@Param("storeId") Long storeId, 
                                        @Param("productId") Long productId, 
                                        @Param("today") LocalDate today);

    @Query("SELECT b FROM InventoryBatch b WHERE b.store.id = :storeId AND b.product.id = :productId AND b.status = 'ACTIVE'")
    List<InventoryBatch> findActiveBatchesByProduct(@Param("storeId") Long storeId, @Param("productId") Long productId);

    @Query("SELECT b FROM InventoryBatch b WHERE b.store.id = :storeId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(b.product.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.batchNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.product.barcode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR b.status = :status)")
    Page<InventoryBatch> searchBatches(@Param("storeId") Long storeId,
                                      @Param("search") String search,
                                      @Param("status") String status,
                                      Pageable pageable);

    @Query("SELECT b FROM InventoryBatch b WHERE b.store.id = :storeId AND b.quantity > 0 " +
           "AND b.expiryDate IS NOT NULL AND b.expiryDate >= :today AND b.expiryDate <= :thresholdDate " +
           "ORDER BY b.expiryDate ASC")
    List<InventoryBatch> findExpiringBatches(@Param("storeId") Long storeId, 
                                           @Param("today") LocalDate today, 
                                           @Param("thresholdDate") LocalDate thresholdDate);

    @Query("SELECT b FROM InventoryBatch b WHERE b.store.id = :storeId AND b.quantity > 0 " +
           "AND b.expiryDate IS NOT NULL AND b.expiryDate < :today " +
           "ORDER BY b.expiryDate ASC")
    List<InventoryBatch> findExpiredBatches(@Param("storeId") Long storeId, @Param("today") LocalDate today);

    @Query("SELECT b.product.id, SUM(b.quantity) FROM InventoryBatch b " +
           "WHERE b.store.id = :storeId AND b.status = 'ACTIVE' " +
           "GROUP BY b.product.id")
    List<Object[]> getTotalStockPerProduct(@Param("storeId") Long storeId);
}
