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

    @PostMapping("{id}/order")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String orderVoucher(Model model,
                               @AuthenticationPrincipal User user,
                               @PathVariable String id,
                               @PageableDefault(size = 10, page = 0) Pageable pageable) {

        voucherService.order(id, user.getId().toString());

        model.addAttribute("vouchers", voucherService.findWithFilers(new VoucherFilerRequest(), pageable));
        model.addAttribute("message", "Successfully ordered voucher!");

        return "fragments/voucher-list :: voucher-list-fragment";
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("@auth.isUserObject(#id)")
    public String getUserVouchers(@PathVariable String id,
                                  PersonalVoucherFilterRequest filer,
                                  @PageableDefault(size = 10, page = 0) Pageable pageable,
                                  Model model) {

        filer.setUserId(UUID.fromString(id));

        model.addAttribute("vouchers", voucherService.findAllByUserId(filer, pageable));

        return "fragments/voucher-profile-list :: voucher-profile-list-fragment";
    }
}
