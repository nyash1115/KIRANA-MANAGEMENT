package com.kirana.repository;

import com.kirana.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);
    Optional<Product> findByBarcodeIgnoreCase(String barcode);
    Optional<Product> findBySku(String sku);
    Optional<Product> findBySkuIgnoreCase(String sku);
    boolean existsByBarcode(String barcode);
    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.active = true " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> searchProducts(@Param("search") String search, 
                                 @Param("categoryId") Long categoryId, 
                                 Pageable pageable);
}
