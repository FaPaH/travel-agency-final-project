package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.PaginatedResponse;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.service.VoucherService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    Voucher toVoucher(VoucherDTO voucherDTO);

    @Mapping(target = "userId", source = "user.id")
    VoucherDTO toVoucherDTO(Voucher voucher);
}
