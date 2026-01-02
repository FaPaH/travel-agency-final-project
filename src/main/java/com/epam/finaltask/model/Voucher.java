package com.epam.finaltask.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@ToString(exclude = {"user"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "vouchers")
public class Voucher extends BaseEntity{

    @Id
    @Column(name = "id")
    @UuidGenerator
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_tour_type", columnDefinition = "tour_type")
    private TourType tourType;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_transfer_type", columnDefinition = "transfer_type")
    private TransferType transferType;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_hotel_type", columnDefinition = "hotel_type")
    private HotelType hotelType;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_status_type", columnDefinition = "status_type")
    private VoucherStatus status;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "eviction_date")
    private LocalDate evictionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ColumnDefault("false")
    @Column(name = "is_hot")
    private Boolean isHot;
}
