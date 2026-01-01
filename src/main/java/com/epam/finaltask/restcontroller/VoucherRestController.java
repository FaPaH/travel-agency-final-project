package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.AdminVoucherFilter;
import com.epam.finaltask.model.PaginatedResponse;
import com.epam.finaltask.model.VoucherFiler;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> getFilteredVouchers(VoucherFiler filer,
                                                                        @PageableDefault(size = 10, page = 0) Pageable pageable) {

        return ResponseEntity.ok().body(voucherService.findWithFilers(filer, pageable));
    }

    @GetMapping("/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> getAdminFilteredVouchers(AdminVoucherFilter adminFiler,
                                                                        @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return ResponseEntity.ok().body(voucherService.findWithFilers(adminFiler, pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PaginatedResponse<VoucherDTO>> findAllByUserId(@PathVariable String userId) {

        return null;
    }

//    @GetMapping
//    public ResponseEntity<Map<String, Object>> findAll() {
//        List<VoucherDTO> vouchers = voucherService.findAll();
//        return ResponseEntity.ok(Map.of("results", vouchers));
//    }
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<Map<String, Object>> findAllByUserId(@PathVariable String userId) {
//        List<VoucherDTO> vouchers = voucherService.findAllByUserId(userId);
//        return ResponseEntity.ok(Map.of("results", vouchers));
//    }
//
//    @PostMapping
//    public ResponseEntity<Map<String, Object>> createVoucher(@RequestBody VoucherDTO voucherDTO) {
//        voucherService.create(voucherDTO);
//        Map<String, Object> response = new HashMap<>();
//        response.put("statusCode", "OK");
//        response.put("statusMessage", "Voucher is successfully created");
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }
//
//    @PatchMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> updateVoucher(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
//        voucherService.update(id, voucherDTO);
//        Map<String, Object> response = new HashMap<>();
//        response.put("statusCode", "OK");
//        response.put("statusMessage", "Voucher is successfully updated");
//        return ResponseEntity.ok(response);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> deleteVoucherById(@PathVariable String id) {
//        voucherService.delete(id);
//        Map<String, Object> response = new HashMap<>();
//        response.put("statusCode", "OK");
//        response.put("statusMessage", "Voucher with Id " + id + " has been deleted");
//        return ResponseEntity.ok(response);
//    }
//
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<Map<String, Object>> changeVoucherStatus(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
//        voucherService.changeHotStatus(id, voucherDTO);
//        Map<String, Object> response = new HashMap<>();
//        response.put("statusCode", "OK");
//        response.put("statusMessage", "Voucher status is successfully changed");
//        return ResponseEntity.ok(response);
//    }
}