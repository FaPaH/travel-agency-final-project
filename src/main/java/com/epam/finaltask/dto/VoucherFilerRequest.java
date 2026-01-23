package com.epam.finaltask.dto;

import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherFilerRequest {

    private List<TourType> tours;
    private List<TransferType> transfers;
    private List<HotelType> hotels;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortField;
    private String sortDirection;

    public void setSortField(String sortField) {
        if (sortField != null && sortField.contains(",")) {
            this.sortField = sortField.split(",")[0].trim();
        } else {
            this.sortField = sortField;
        }
    }

    public void setSortDirection(String sortDirection) {
        if (sortDirection != null && sortDirection.contains(",")) {
            this.sortDirection = sortDirection.split(",")[0].trim();
        } else {
            this.sortDirection = sortDirection;
        }
    }
}
