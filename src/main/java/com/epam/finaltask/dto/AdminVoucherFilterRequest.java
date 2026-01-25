package com.epam.finaltask.dto;

import com.epam.finaltask.model.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminVoucherFilterRequest extends VoucherFilerRequest {

    private String voucherId;
    private String title;
    private List<VoucherStatus> statuses;
    private Boolean isHot;
}
