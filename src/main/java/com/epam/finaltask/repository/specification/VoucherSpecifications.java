package com.epam.finaltask.repository.specification;

import com.epam.finaltask.model.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VoucherSpecifications {

    public static Specification<Voucher> withFilters(VoucherFiler filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNull(root.get("status")));

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

            query.orderBy(
                    cb.desc(root.get("isHot")),
                    cb.desc(root.get("createdAt")),
                    cb.asc(root.get("title"))
            );

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
