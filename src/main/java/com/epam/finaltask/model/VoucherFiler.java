package com.epam.finaltask.model;

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
public class VoucherFiler {

    private List<VoucherStatus> statuses;
    private List<TourType> tours;
    private List<TransferType> transfers;
    private List<HotelType> hotels;
    private Boolean isHot;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
