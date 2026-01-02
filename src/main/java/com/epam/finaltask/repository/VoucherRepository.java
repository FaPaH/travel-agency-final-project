package com.epam.finaltask.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<Voucher, UUID>, JpaSpecificationExecutor<Voucher> {
    Page<Voucher> findAllByUserId(UUID userId, Pageable pageable);
}
