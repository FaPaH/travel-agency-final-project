package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.AdminVoucherFilterRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.UserService;
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
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VoucherService voucherService;
    private final UserService userService;

    @GetMapping("/users")
    public String getAllUsers(Model model, Pageable pageable) {
        model.addAttribute("users", userService.getAllUsers(pageable));

        return "fragments/user-admin-list :: user-list-fragment";
    }

    @PostMapping("/users/toggle-status")
    public String blockUser(UserDTO userDto, Model model, Pageable pageable) {
        userService.changeAccountStatus(userDto);

        model.addAttribute("users", userService.getAllUsers(pageable));

        return "fragments/user-admin-list :: user-list-fragment";
    }

    @GetMapping
    public String admin(Model model) {
        return "admin/admin-page";
    }

    @DeleteMapping("/vouchers/delete/{id}")
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

    @PatchMapping("/user/block")
    public ResponseEntity<UserDTO> changeAccountStatus(@RequestBody UserDTO userDTO) {

        return ResponseEntity.ok().body(userService.changeAccountStatus(userDTO));
    }
}
