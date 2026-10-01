package com.kirana.repository;

import com.kirana.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("SELECT si.product.name, SUM(si.quantity), SUM(si.lineTotal) " +
           "FROM SaleItem si WHERE si.sale.store.id = :storeId AND si.sale.status = 'COMPLETED' " +
           "AND si.sale.saleDate >= :startDate AND si.sale.saleDate <= :endDate " +
           "GROUP BY si.product.name " +
           "ORDER BY SUM(si.quantity) DESC")
    List<Object[]> getTopSellingProducts(@Param("storeId") Long storeId,
                                        @Param("startDate") Instant startDate,
                                        @Param("endDate") Instant endDate,
                                        Pageable pageable);

    @Query("SELECT si.product.category.name, SUM(si.lineTotal) " +
           "FROM SaleItem si WHERE si.sale.store.id = :storeId AND si.sale.status = 'COMPLETED' " +
           "AND si.sale.saleDate >= :startDate AND si.sale.saleDate <= :endDate " +
           "GROUP BY si.product.category.name " +
           "ORDER BY SUM(si.lineTotal) DESC")
    List<Object[]> getCategoryWiseSales(@Param("storeId") Long storeId,
                                       @Param("startDate") Instant startDate,
                                       @Param("endDate") Instant endDate);
}
