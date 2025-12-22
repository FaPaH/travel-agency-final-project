package com.epam.finaltask.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class VoucherDTO {

    private String id;

    private String title;

    private String description;

    private Double price;

    private String tourType;

    private String transferType;

    private String hotelType;

    private String status;

    private LocalDate arrivalDate;

    private LocalDate evictionDate;

    private UUID userId;

    private Boolean isHot;

}
