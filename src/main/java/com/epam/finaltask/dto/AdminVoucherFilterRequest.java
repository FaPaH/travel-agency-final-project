package com.epam.finaltask.dto;

import com.epam.finaltask.model.VoucherStatus;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminVoucherFilterRequest extends VoucherFilerRequest {

    //TODO: Change admin filter service method for searching vouchers by userId if it present

    private String userId;
    private List<VoucherStatus> status;
}
