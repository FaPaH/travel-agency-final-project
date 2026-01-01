package com.epam.finaltask.mapper;

import com.epam.finaltask.model.PaginatedResponse;
import org.springframework.data.domain.Page;

public class PaginationMapper {

    public static <T> PaginatedResponse<T> toPaginatedResponse(Page<T> page) {
        PaginatedResponse<T> response = new PaginatedResponse<>();

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
