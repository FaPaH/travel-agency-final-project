package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.TopUpRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
public class UserController {

    //TODO: reformating api controllers, tests

    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "user/dashboard";
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("@auth.isUserObject(#id)")
    public String profile(@PathVariable String id,
                          Model model) {
        UserDTO userDto = userService.getUserById(UUID.fromString(id));

        model.addAttribute("user", userDto);

        return "fragments/user-profile :: user-info-fragment";
    }

    @GetMapping("/profile/{id}/edit")
    @PreAuthorize("@auth.isUserObject(#id)")
    public String editProfile(@PathVariable String id,
                              Model model) {
        UserDTO userDto = userService.getUserById(UUID.fromString(id));

        model.addAttribute("user", userDto);

        return "fragments/user-profile :: profile-edit-fragment";
    }

    @PostMapping("/profile/{id}/update")
    @PreAuthorize("@auth.isUserObject(#userDto.id)")
    public String updateProfile(@Valid @ModelAttribute("user") UserDTO userDto,
                                BindingResult bindingResult,
                                HttpServletResponse response,
                                Model model) {
        if (bindingResult.hasErrors()) {
            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return "fragments/user-profile :: profile-edit-fragment";
        }

        UserDTO updatedUser = userService.updateUser(userDto.getUsername(), userDto);

        model.addAttribute("user", updatedUser);

        return "fragments/user-profile :: user-info-fragment";
    }

    @GetMapping("/profile/{id}/balance")
    @PreAuthorize("@auth.isUserObject(#id)")
    public String balance(@AuthenticationPrincipal User user,
                          @PathVariable String id,
                          Model model) {

        UserDTO userDto = userService.getUserById(user.getId());

        model.addAttribute("topup", new TopUpRequest());
        model.addAttribute("user", userDto);

        return "fragments/user-profile :: profile-balance-fragment";
    }

    @PostMapping("/profile/{id}/balance/top-up")
    @PreAuthorize("@auth.isUserObject(#id)")
    public String updateBalance(@PathVariable String id,
                                @ModelAttribute("topup") @Valid TopUpRequest topUpRequest,
                                BindingResult bindingResult,
                                HttpServletResponse response,
                                Model model) {
        if (bindingResult.hasErrors()) {
            UserDTO userDto = userService.getUserById(UUID.fromString(id));

            model.addAttribute("user", userDto);

            response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());

            return "fragments/user-profile :: profile-balance-fragment";
        }

        UserDTO userDto = userService.changeBalance(id, topUpRequest.getAmount());

        model.addAttribute("user", userDto);

        return "fragments/user-profile :: profile-balance-fragment";
    }
}
