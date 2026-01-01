package com.epam.finaltask.service;

import java.math.BigDecimal;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);
    PaginatedResponse<VoucherDTO> findAllByUserId(String userId, Pageable pageable);

    PaginatedResponse<VoucherDTO> findWithFilers(VoucherFiler voucherFiler, Pageable pageable);
}
