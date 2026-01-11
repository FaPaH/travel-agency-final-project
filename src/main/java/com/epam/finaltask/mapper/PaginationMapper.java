package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.model.VoucherPaginatedResponse;
import org.springframework.data.domain.Page;

public class PaginationMapper {

    public static <T> PaginatedResponse<T> toPaginatedResponse(Page<T> page) {
        return fill(page, new PaginatedResponse<>());
    }

    public static VoucherPaginatedResponse toVoucherResponse(Page<VoucherDTO> page) {
        return fill(page, new VoucherPaginatedResponse());
    }

    private static <T, R extends PaginatedResponse<T>> R fill(Page<T> page, R response) {
        response.setData(page.getContent());
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }
}
