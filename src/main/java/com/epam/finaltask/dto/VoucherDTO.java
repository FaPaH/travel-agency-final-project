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
@ValidDateRange(message = "{validation.voucher.date.range}")
public class VoucherDTO {

    @ToString.Include
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            message = "{validation.voucher.id.format}")
    private String id;

    @NotBlank(message = "{validation.voucher.title.required}")
    @ToString.Include
    private String title;

    @NotBlank(message = "{validation.voucher.description.required}")
    private String description;

    @ToString.Include
    @Positive(message = "{validation.voucher.price.positive}")
    private BigDecimal price;

    @NotNull(message = "{validation.required}")
    @EnumValidator(enumClass = TourType.class, message = "{validation.enum.invalid}")
    private String tourType;

    @NotNull(message = "{validation.required}")
    @EnumValidator(enumClass = TransferType.class, message = "{validation.enum.invalid}")
    private String transferType;

    @NotNull(message = "{validation.required}")
    @EnumValidator(enumClass = HotelType.class, message = "{validation.enum.invalid}")
    private String hotelType;

    @NotNull(message = "{validation.required}")
    @EnumValidator(enumClass = VoucherStatus.class, message = "{validation.enum.invalid}")
    private String status;

    @NotNull(message = "{validation.voucher.date.required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate arrivalDate;

    @NotNull(message = "{validation.voucher.date.required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate evictionDate;

    @ToString.Include
    private UUID userId;

    @ToString.Include
    private Boolean isHot = false;

}
