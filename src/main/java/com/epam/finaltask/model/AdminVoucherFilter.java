package com.epam.finaltask.model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminVoucherFilter extends VoucherFiler {
    private List<VoucherStatus> status;
}
