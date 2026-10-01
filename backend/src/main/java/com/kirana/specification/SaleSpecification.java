package com.kirana.specification;

import com.kirana.entity.Sale;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SaleSpecification {

    public static Specification<Sale> withFilters(
            Long storeId,
            String invoiceNumber,
            Long customerId,
            String status,
            Instant startDate,
            Instant endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (storeId != null) {
                predicates.add(cb.equal(root.get("store").get("id"), storeId));
            }
            if (invoiceNumber != null && !invoiceNumber.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("invoiceNumber")), "%" + invoiceNumber.trim().toLowerCase() + "%"));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("saleDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("saleDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
