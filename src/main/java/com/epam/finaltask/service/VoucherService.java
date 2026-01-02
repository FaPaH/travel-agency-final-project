package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherStatusRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeStatus(String id, VoucherStatusRequest statusRequest);;
    VoucherPaginatedResponse findAllByUserId(String userId, Pageable pageable);

    VoucherPaginatedResponse findWithFilers(VoucherFiler voucherFiler, Pageable pageable);
}
