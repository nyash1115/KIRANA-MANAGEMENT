package com.kirana.repository;

import com.kirana.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    @Query("SELECT a FROM AuditLog a WHERE (:storeId IS NULL OR a.store.id = :storeId) " +
           "AND (:entityName IS NULL OR a.entityName = :entityName) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findAuditLogsWithFilter(@Param("storeId") Long storeId,
                                         @Param("entityName") String entityName,
                                         @Param("action") String action,
                                         Pageable pageable);
}
