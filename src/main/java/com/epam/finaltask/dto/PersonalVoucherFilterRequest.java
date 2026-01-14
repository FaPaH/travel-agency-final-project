package com.epam.finaltask.dto;

import com.epam.finaltask.model.VoucherStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonalVoucherFilterRequest extends VoucherFilerRequest {

    private UUID userId;
    private List<VoucherStatus> statuses;
}
