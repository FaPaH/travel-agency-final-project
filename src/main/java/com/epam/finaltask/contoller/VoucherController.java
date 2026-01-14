package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.PersonalVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherFilerRequest;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
