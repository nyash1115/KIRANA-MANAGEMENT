package com.kirana.repository;

import com.kirana.entity.SaleReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {
    Optional<SaleReturn> findByReturnNumber(String returnNumber);

    @Query("SELECT sr FROM SaleReturn sr WHERE sr.store.id = :storeId ORDER BY sr.returnDate DESC")
    Page<SaleReturn> findByStoreIdOrderByReturnDateDesc(@Param("storeId") Long storeId, Pageable pageable);
}
