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
    private Boolean isHot;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
