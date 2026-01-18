package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/users")
    public String getAllUsers(Model model,
                              Pageable pageable) {

        model.addAttribute("users", userService.getAllUsers(pageable));

        return "fragments/user-admin-list :: user-list-fragment";
    }

    @PostMapping("/users/toggle-status")
    public String blockUser(@ModelAttribute @Valid UserDTO userDto,
                            Model model,
                            Pageable pageable) {

        userService.changeAccountStatus(userDto);

        model.addAttribute("users", userService.getAllUsers(pageable));

        return "fragments/user-admin-list :: user-list-fragment";
    }
}
