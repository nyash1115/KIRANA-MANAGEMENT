package com.kirana.specification;

import com.kirana.entity.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    public static Specification<AuditLog> withFilters(Long storeId, String entityName, String action) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (storeId != null) {
                predicates.add(cb.equal(root.get("store").get("id"), storeId));
            }
            if (entityName != null && !entityName.isBlank()) {
                predicates.add(cb.equal(root.get("entityName"), entityName.trim()));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
