package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopUpRequest {

    @NotNull(message = "{validation.payment.amount.required}")
    @Positive(message = "{validation.payment.amount.positive}")
    private BigDecimal amount;
}
