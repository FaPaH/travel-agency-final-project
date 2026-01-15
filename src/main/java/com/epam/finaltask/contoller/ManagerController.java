package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherStatusRequest;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String getManagerPage(Model model) {

        return "manager/manager-page";
    }

    @GetMapping("/vouchers")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String getVouchers(Model model,
                              AdminVoucherFilterRequest filterRequest,
                              @PageableDefault(size = 10, page = 0) Pageable pageable) {

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));

        return "fragments/voucher-manager-list :: voucher-list-fragment";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String editRow(@PathVariable UUID id, Model model) {
        VoucherDTO voucher = voucherService.getById(id.toString());
        model.addAttribute("voucher", voucher);
        return "fragments/voucher-manager-list :: voucher-row-edit";
    }

    @GetMapping("/row/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String getRow(@PathVariable UUID id, Model model) {
        VoucherDTO voucher = voucherService.getById(id.toString());
        model.addAttribute("voucher", voucher);
        return "fragments/voucher-manager-list :: voucher-row-view";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String updateVoucher(@PathVariable UUID id,
                                VoucherStatusRequest request,
                                AdminVoucherFilterRequest filterRequest,
                                @PageableDefault(size = 10, page = 0) Pageable pageable,
                                Model model) {

        voucherService.changeStatus(id.toString(), request);

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));

        return "fragments/voucher-manager-list :: voucher-list-fragment";
    }
}
