package com.epam.finaltask.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class VoucherDTO {

    @ToString.Include
    private String id;

    @ToString.Include
    private String title;

    private String description;

    @ToString.Include
    private BigDecimal price;

    private String tourType;

    private String transferType;

    private String hotelType;

    private String status;

    private LocalDate arrivalDate;

    private LocalDate evictionDate;

    @ToString.Include
    private UUID userId;

    @ToString.Include
    private Boolean isHot = false;

}
