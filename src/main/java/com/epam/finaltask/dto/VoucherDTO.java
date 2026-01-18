package com.epam.finaltask.dto;

import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.validation.annotation.EnumValidator;
import com.epam.finaltask.validation.annotation.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@ToString(onlyExplicitlyIncluded = true)
@ValidDateRange
public class VoucherDTO {

    @ToString.Include
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                message = "Invalid format of id")
    private String id;

    @NotBlank
    @ToString.Include
    private String title;

    @NotBlank
    private String description;

    @ToString.Include
    @Positive(message = "Price must be more than zero")
    private BigDecimal price;

    @NotNull
    @EnumValidator(enumClass = TourType.class)
    private String tourType;

    @NotNull
    @EnumValidator(enumClass = TransferType.class)
    private String transferType;

    @NotNull
    @EnumValidator(enumClass = HotelType.class)
    private String hotelType;

    @NotNull
    @EnumValidator(enumClass = VoucherStatus.class)
    private String status;

   @NotNull
   @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate evictionDate;

    @ToString.Include
    private UUID userId;

    @ToString.Include
    private Boolean isHot = false;

}
