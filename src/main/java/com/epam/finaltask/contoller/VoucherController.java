package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.VoucherFilerRequest;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
}
