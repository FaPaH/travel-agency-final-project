package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    VoucherDTO getById(String id);
    void delete(String voucherId);
    VoucherDTO changeStatus(String id, VoucherStatusRequest statusRequest);;
    VoucherPaginatedResponse findAllByUserId(PersonalVoucherFilterRequest filterRequest, Pageable pageable);

    VoucherPaginatedResponse findWithFilers(VoucherFilerRequest voucherFilerRequest, Pageable pageable);
}
