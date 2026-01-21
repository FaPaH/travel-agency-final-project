package com.epam.finaltask.dto;

import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.validation.annotation.EnumValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherStatusRequest {

    private Boolean isHot;

    @EnumValidator(enumClass = VoucherStatus.class, message = "{validation.enum.invalid}")
    private String voucherStatus;
}
