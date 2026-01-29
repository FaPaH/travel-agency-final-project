package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final VoucherService voucherService;
    private final UserService userService;

    @GetMapping("/vouchers")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> getAdminFilteredVouchers(PersonalVoucherFilterRequest adminFiler,
                                                                                  @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return ResponseEntity.ok().body(voucherService.findWithFilers(adminFiler, pageable));
    }

    @DeleteMapping("vouchers/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable String id) {
        voucherService.delete(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/vouchers/create")
    public ResponseEntity<VoucherDTO> createVoucher(@RequestBody @Valid VoucherDTO voucherDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.create(voucherDTO));
    }

    @PostMapping("vouchers/update/{id}")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable String id,
                                                    @RequestBody @Valid VoucherDTO voucherDTO) {
        return ResponseEntity.ok().body(voucherService.update(id, voucherDTO));
    }

    @GetMapping("/users")
    public ResponseEntity<PaginatedResponse<UserDTO>> getAllUsers(@PageableDefault(size = 10, page = 0) Pageable pageable) {

        return ResponseEntity.ok().body(userService.getAllUsers(pageable));
    }


    @PostMapping("/users/toggle-status")
    public ResponseEntity<UserDTO> blockUser(@ModelAttribute @Valid BlockUserRequest blockRequest) {
        return ResponseEntity.ok().body(userService.changeAccountStatus(blockRequest.getUsername()));
    }
}
