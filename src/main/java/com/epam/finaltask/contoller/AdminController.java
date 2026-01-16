package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.VoucherFilerRequest;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VoucherService voucherService;

    @GetMapping("/dashboard")
    public String admin(Model model) {
        return "admin/admin-page";
    }

    @GetMapping("/vouchers")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String getVouchersAdmin(Model model,
                                   AdminVoucherFilterRequest filterRequest,
                                   @PageableDefault(size = 10, page = 0) Pageable pageable) {

        model.addAttribute("vouchers", voucherService.findWithFilers(filterRequest, pageable));

        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }

    @DeleteMapping("/vouchers/{id}")
    @ResponseBody
    public void deleteVoucher(@PathVariable UUID id) {
        voucherService.delete(id.toString());
    }

    @GetMapping("/vouchers/create")
    public String createForm(Model model) {
        model.addAttribute("voucher", new VoucherDTO());
        return "fragments/voucher-admin-list :: create-fragment";
    }

    @GetMapping("/row/{id}")
    public String getRow(@PathVariable UUID id, Model model) {
        VoucherDTO voucher = voucherService.getById(id.toString());
        model.addAttribute("voucher", voucher);
        return "fragments/voucher-admin-list :: voucher-row-view";
    }

    @PostMapping("/vouchers/create")
    public String processCreateVoucher(VoucherDTO voucherDTO,
                                       Model model,
                                       @PageableDefault(size = 10) Pageable pageable) {
        voucherService.create(voucherDTO);

        model.addAttribute("vouchers", voucherService.findWithFilers(new AdminVoucherFilterRequest(), pageable));

        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }

    @GetMapping("/vouchers/edit/{id}")
    public String editFullRow(@PathVariable UUID id, Model model) {
        VoucherDTO voucher = voucherService.getById(id.toString());
        model.addAttribute("voucher", voucher);
        return "fragments/voucher-admin-list :: voucher-edit";
    }

    @PostMapping("/vouchers/update/{id}")
    public String updateVoucher(@PathVariable String id,
                                VoucherDTO voucherDTO,
                                @PageableDefault(size = 10, page = 0) Pageable pageable,
                                Model model) {

        voucherService.update(id, voucherDTO);

        model.addAttribute("vouchers", voucherService.findWithFilers(new VoucherFilerRequest(), pageable));

        return "fragments/voucher-admin-list :: voucher-list-fragment";
    }
}
