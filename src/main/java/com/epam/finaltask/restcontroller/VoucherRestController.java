package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.VoucherStatusRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.PersonalVoucherFilterRequest;
import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.model.User;
import com.epam.finaltask.dto.VoucherFilerRequest;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> getFilteredVouchers(VoucherFilerRequest filer,
                                                                             @PageableDefault(size = 10, page = 0) Pageable pageable) {

        return ResponseEntity.ok().body(voucherService.findWithFilers(filer, pageable));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> getAdminFilteredVouchers(PersonalVoucherFilterRequest adminFiler,
                                                                                  @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return ResponseEntity.ok().body(voucherService.findWithFilers(adminFiler, pageable));
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> adminFindAllByUserId(@PathVariable String userId,
                                                                              @PageableDefault(size = 20, page = 0) Pageable pageable) {

        //voucherService.findAllByUserId(userId, pageable)

        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> createVoucher(@RequestBody VoucherDTO voucherDTO) {
        voucherService.create(voucherDTO);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("admin/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> updateVoucher(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
        voucherService.update(id, voucherDTO);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("admin/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteVoucherById(@PathVariable String id) {
        voucherService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("manager/status/{id}")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<Void> changeVoucherStatus(@PathVariable String id,
                                                    @RequestBody VoucherStatusRequest statusRequest) {
        voucherService.changeStatus(id, statusRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("order/{voucherId}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<VoucherDTO> orderVoucher(@AuthenticationPrincipal User user,
                                                                      @PathVariable String voucherId) {
        voucherService.order(voucherId, user.getId().toString());

        return ResponseEntity.ok().build();
    }
}