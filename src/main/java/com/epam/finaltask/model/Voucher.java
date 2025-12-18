package com.epam.finaltask.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table
public class Voucher {

    @Id
    private UUID id;

    private String title;

    private String description;

    private Double price;

    private TourType tourType;

    private TransferType transferType;

    private HotelType hotelType;

    private VoucherStatus status;

    private LocalDate arrivalDate;

    private LocalDate evictionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean isHot;


}
