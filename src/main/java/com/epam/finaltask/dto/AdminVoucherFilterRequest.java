package com.epam.finaltask.dto;

import com.epam.finaltask.model.VoucherStatus;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminVoucherFilterRequest extends VoucherFilerRequest {

    private List<VoucherStatus> statuses;
}
