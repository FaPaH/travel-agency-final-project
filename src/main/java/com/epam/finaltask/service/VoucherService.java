package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.List;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);
    Page<VoucherDTO> findAllByUserId(String userId, Pageable pageable);

    Page<VoucherDTO> findAllByTourType(TourType tourType, Pageable pageable);
    Page<VoucherDTO> findAllByTransferType(String transferType, Pageable pageable);
    Page<VoucherDTO> findAllByPrice(BigDecimal price, Pageable pageable);
    Page<VoucherDTO> findAllByHotelType(HotelType hotelType, Pageable pageable);

    PaginatedResponse<VoucherDTO> findWithFilers(VoucherFiler voucherFiler, Pageable pageable);

    Page<VoucherDTO> findAll(Pageable pageable);
}
