package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.dto.PersonalVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherStatusRequest;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerRestController {

    private final VoucherService voucherService;

    @GetMapping("/vouchers")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> getManagerFilteredVouchers(PersonalVoucherFilterRequest adminFiler,
                                                                                    @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return ResponseEntity.ok().body(voucherService.findWithFilers(adminFiler, pageable));
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable String id,
                                                    @RequestBody @Valid VoucherStatusRequest statusRequest) {
        return ResponseEntity.ok().body(voucherService.changeStatus(id, statusRequest));
    }
}
