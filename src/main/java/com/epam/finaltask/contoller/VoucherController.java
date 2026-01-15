package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public String getFilteredVouchers(Model model,
                                      VoucherFilerRequest filer,
                                      @PageableDefault(size = 10, page = 0) Pageable pageable) {

        model.addAttribute("vouchers", voucherService.findWithFilers(filer, pageable));

        return "fragments/voucher-list :: voucher-list-fragment";
    }


    @GetMapping("/user")
    public String getUserVouchers(Model model,
                                  PersonalVoucherFilterRequest filer,
                                  @PageableDefault(size = 10, page = 0) Pageable pageable,
                                  @AuthenticationPrincipal User user) {

        filer.setUserId(user.getId());
        model.addAttribute("vouchers", voucherService.findAllByUserId(filer, pageable));

        return "fragments/voucher-profile-list :: voucher-profile-list-fragment";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String getVouchersManager(Model model,
                                     AdminVoucherFilterRequest filterRequest,
                                     @PageableDefault(size = 10, page = 0) Pageable pageable) {

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));

        return "fragments/voucher-manager-list :: voucher-list-fragment";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String getVouchersAdmin(Model model,
                                   AdminVoucherFilterRequest filterRequest,
                                   @PageableDefault(size = 10, page = 0) Pageable pageable) {

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));

        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String updateVoucher(@PathVariable String id,
                                VoucherDTO voucherDTO,
                                @PageableDefault(size = 10, page = 0) Pageable pageable,
                                Model model) {

        voucherService.update(id, voucherDTO);

        model.addAttribute("vouchers", voucherService.findWithFilers(new VoucherFilerRequest(), pageable));

        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }
}
