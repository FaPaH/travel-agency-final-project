package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopUpRequest {

    @NotNull(message = "Sum cant be empty")
    @PositiveOrZero(message = "Sum must be more than 0 or 0")
    private BigDecimal amount;
}
