package com.epam.finaltask.model;

import com.epam.finaltask.dto.VoucherFilerRequest;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminVoucherFilterRequest extends VoucherFilerRequest {
    private List<VoucherStatus> status;
}
