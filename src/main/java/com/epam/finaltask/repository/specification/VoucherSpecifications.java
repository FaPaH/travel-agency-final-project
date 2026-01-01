package com.epam.finaltask.repository.specification;

import com.epam.finaltask.model.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VoucherSpecifications {

    public static Specification<Voucher> withFilters(VoucherFiler filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter instanceof AdminVoucherFilter adminFilter) {
                if (adminFilter.getStatuses() != null && !adminFilter.getStatuses().isEmpty()) {
                    predicates.add(root.get("status").in(adminFilter.getStatuses()));
                }
            } else {
                predicates.add(cb.isNull(root.get("status")));
            }

            addCommonPredicates(predicates, root, cb, filter);

            query.orderBy(
                    cb.desc(root.get("isHot")),
                    cb.desc(root.get("createdAt")),
                    cb.asc(root.get("title"))
            );

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addCommonPredicates(List<Predicate> predicates, Root<Voucher> root, CriteriaBuilder cb, VoucherFiler filter) {

        if (filter.getTours() != null && !filter.getTours().isEmpty()) {
            predicates.add(root.get("tourType").in(filter.getTours()));
        }
        if (filter.getTransfers() != null && !filter.getTransfers().isEmpty()) {
            predicates.add(root.get("transferType").in(filter.getTransfers()));
        }
        if (filter.getHotels() != null && !filter.getHotels().isEmpty()) {
            predicates.add(root.get("hotelType").in(filter.getHotels()));
        }
        if (filter.getMinPrice() != null) {
            predicates.add(cb.ge(root.get("price"), filter.getMinPrice()));
        }
        if (filter.getMaxPrice() != null) {
            predicates.add(cb.le(root.get("price"), filter.getMaxPrice()));
        }
    }
}
