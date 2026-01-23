package com.epam.finaltask.repository.specification;

import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.PersonalVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherFilerRequest;
import com.epam.finaltask.model.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class VoucherSpecifications {

    public static Specification<Voucher> withFilters(VoucherFilerRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter instanceof PersonalVoucherFilterRequest personalFilter) {
                if (personalFilter.getStatuses() != null && !personalFilter.getStatuses().isEmpty()) {
                    predicates.add(root.get("status").in(personalFilter.getStatuses()));
                }
                if (personalFilter.getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), personalFilter.getUserId()));
                }
            } else if (filter instanceof AdminVoucherFilterRequest adminFilter) {
                if (adminFilter.getStatuses() != null && !adminFilter.getStatuses().isEmpty()) {
                    predicates.add(root.get("status").in(adminFilter.getStatuses()));
                }
                if (adminFilter.getVoucherId() != null && !adminFilter.getVoucherId().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("id").as(String.class)), "%" + adminFilter.getVoucherId().toLowerCase() + "%"));
                }
                if (adminFilter.getIsHot() != null) {
                    predicates.add(cb.equal(root.get("isHot"), adminFilter.getIsHot()));
                }
                if (adminFilter.getTitle() != null && !adminFilter.getTitle().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + adminFilter.getTitle().toLowerCase() + "%"));
                }
            } else {
                predicates.add(cb.equal(root.get("status"), VoucherStatus.CREATED));
            }

            addCommonPredicates(predicates, root, cb, filter);

            String field = filter.getSortField();
            String direction = filter.getSortDirection();
            boolean isAsc = "asc".equalsIgnoreCase(direction);

            if ("price".equalsIgnoreCase(field)) {
                if (isAsc) {
                    query.orderBy(cb.asc(root.get("price")));
                } else {
                    query.orderBy(cb.desc(root.get("price")));
                }
            }
            else if ("title".equalsIgnoreCase(field)) {
                if (isAsc) {
                    query.orderBy(cb.asc(root.get("title")));
                } else {
                    query.orderBy(cb.desc(root.get("title")));
                }
            }
            else {
                if (filter instanceof AdminVoucherFilterRequest) {
                    query.orderBy(
                            cb.desc(root.get("isHot")),
                            cb.asc(root.get("status")),
                            cb.asc(root.get("title")),
                            cb.desc(root.get("updatedAt"))
                    );
                } else if (filter instanceof PersonalVoucherFilterRequest) {
                    query.orderBy(
                            cb.desc(root.get("updatedAt")),
                            cb.asc(root.get("status")),
                            cb.asc(root.get("title"))
                    );
                } else {
                    query.orderBy(
                            cb.desc(root.get("isHot")),
                            cb.desc(root.get("createdAt")),
                            cb.asc(root.get("title"))
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addCommonPredicates(List<Predicate> predicates, Root<Voucher> root, CriteriaBuilder cb, VoucherFilerRequest filter) {
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
