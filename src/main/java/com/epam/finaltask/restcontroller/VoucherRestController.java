package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.PersonalVoucherFilterRequest;
import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.model.User;
import com.epam.finaltask.dto.VoucherFilerRequest;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping("{id}/order")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<VoucherDTO> orderVoucher(@AuthenticationPrincipal User user,
                                                   @PathVariable String id) {

        return ResponseEntity.ok().body(voucherService.order(id, user.getId().toString()));
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("@auth.isUserObject(#id)")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> getUserVouchers(@PathVariable String id,
                                                                         PersonalVoucherFilterRequest filer,
                                                                         @PageableDefault(size = 10, page = 0) Pageable pageable) {

        filer.setUserId(UUID.fromString(id));

        return ResponseEntity.ok().body(voucherService.findAllByUserId(filer, pageable));
    }
}