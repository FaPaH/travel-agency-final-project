package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.TopUpRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        return "user/dashboard";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(@AuthenticationPrincipal User user, Model model) {
        UserDTO userDto = userService.getUserById(user.getId());
        model.addAttribute("user", userDto);

        return "fragments/user-profile :: user-info-fragment";
    }

    @GetMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public String editProfile(@AuthenticationPrincipal User user, Model model) {
        UserDTO userDto = userService.getUserById(user.getId());
        model.addAttribute("user", userDto);

        return "fragments/user-profile :: profile-edit-fragment";
    }

    @PostMapping("/profile/update")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@AuthenticationPrincipal User user, Model model, UserDTO userDto) {
        UserDTO updatedUser = userService.updateUser(user.getUsername(), userDto);
        model.addAttribute("user", updatedUser);

        return "fragments/user-profile :: user-info-fragment";
    }

    @GetMapping("/profile/balance")
    @PreAuthorize("isAuthenticated()")
    public String balance(@AuthenticationPrincipal User user, Model model) {
        UserDTO userDto = userService.getUserById(user.getId());
        model.addAttribute("user", userDto);

        return "fragments/user-profile :: profile-balance-fragment";
    }

    @PostMapping("/profile/balance/top-up")
    @PreAuthorize("isAuthenticated()")
    public String updateBalance(@AuthenticationPrincipal User user, Model model, @Valid TopUpRequest amount) {
        UserDTO userDto = userService.changeBalance(user.getId().toString(), amount.getAmount());
        model.addAttribute("user", userDto);

        return "fragments/user-profile :: profile-balance-fragment";
    }
}
