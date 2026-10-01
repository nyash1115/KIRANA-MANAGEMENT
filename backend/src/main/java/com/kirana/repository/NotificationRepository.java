package com.kirana.repository;

import com.kirana.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStoreIdAndReadFalseOrderByCreatedAtDesc(Long storeId);

    Page<Notification> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    long countByStoreIdAndReadFalse(Long storeId);

    boolean existsByStoreIdAndTypeAndReferenceId(Long storeId, String type, String referenceId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.store.id = :storeId AND n.read = false")
    void markAllAsRead(@Param("storeId") Long storeId);
}
