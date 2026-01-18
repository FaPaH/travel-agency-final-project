package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherStatusRequest;
import com.epam.finaltask.service.VoucherService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerController {

    private final VoucherService voucherService;

    @GetMapping("/dashboard")
    public String getManagerPage(Model model) {
        return "manager/manager-page";
    }

    @GetMapping("/vouchers")
    public String getVouchersManager(Model model,
                                     AdminVoucherFilterRequest filterRequest,
                                     @PageableDefault(size = 10, page = 0) Pageable pageable) {

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));

        return "fragments/voucher-manager-list :: voucher-list-fragment";
    }

    @GetMapping("/edit/{id}")
    public String editRow(@PathVariable UUID id,
                          Model model) {

        VoucherDTO voucher = voucherService.getById(id.toString());

        VoucherStatusRequest request = new VoucherStatusRequest();
        request.setVoucherStatus(voucher.getStatus());
        request.setIsHot(voucher.getIsHot());

        model.addAttribute("voucher", voucher);
        model.addAttribute("voucherStatusRequest", request);

        return "fragments/voucher-manager-list :: voucher-row-edit";
    }

    @GetMapping("/row/{id}")
    public String getRow(@PathVariable UUID id,
                         Model model) {

        VoucherDTO voucher = voucherService.getById(id.toString());

        model.addAttribute("voucher", voucher);

        return "fragments/voucher-manager-list :: voucher-row-view";
    }

    @PostMapping("/update/{id}")
    public String updateVoucher(@PathVariable String id,
                                @ModelAttribute("voucherStatusRequest") @Valid VoucherStatusRequest request,
                                BindingResult bindingResult,
                                HttpServletResponse response,
                                AdminVoucherFilterRequest filterRequest,
                                @PageableDefault(size = 10, page = 0) Pageable pageable,
                                Model model) {


        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());

            VoucherDTO voucher = voucherService.getById(id);
            model.addAttribute("voucher", voucher);

            return "fragments/voucher-manager-list :: voucher-row-view";
        }

        voucherService.changeStatus(id, request);

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));

        return "fragments/voucher-manager-list :: voucher-list-fragment";
    }
}
