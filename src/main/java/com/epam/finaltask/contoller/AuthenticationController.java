package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.ResetService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.util.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {


    private final AuthenticationService authenticationService;
    private final ResetService resetService;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    @GetMapping("/sign-in")
    public String signIn(@ModelAttribute("loginRequest") LoginRequest loginRequest,
                         Model model) {
        return "auth/sign-in";
    }

    @GetMapping("/sign-up")
    public String signUp(@ModelAttribute("registerRequest") RegisterRequest registerRequest,
                         Model model) {
        return "auth/sign-up";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerRequest") @Valid RegisterRequest registerRequest,
                                               HttpServletResponse response) {
        System.out.println(registerRequest);
        AuthResponse authResponse = authenticationService.register(registerRequest);

        saveTokensToCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());

        return "redirect:/index";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") @Valid LoginRequest loginRequest, HttpServletResponse response) {
        AuthResponse authResponse = authenticationService.login(loginRequest);

        saveTokensToCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());

        return "redirect:/index";
    }

    @GetMapping("/reset-password-form")
    public String getResetForm(@AuthenticationPrincipal User user,
                               Model model) {

        ResetRequest resetRequest = new ResetRequest();

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            resetRequest.setEmail(user.getEmail());
            model.addAttribute("hasEmail", true);
        } else {
            model.addAttribute("hasEmail", false);
        }

        model.addAttribute("resetRequest", resetRequest);
        return "fragments/reset-password :: reset-password-fragment";
    }

    @PostMapping("/reset-password")
    public String requestReset(@AuthenticationPrincipal User user,
                               @ModelAttribute("resetRequest") @Valid ResetRequest resetRequest,
                               Model model) {

        userService.updateUser(user.getUsername(), UserDTO.builder()
                .email(resetRequest.getEmail())
                .build());
        resetService.proceedReset(resetRequest.getEmail(), false);

        model.addAttribute("success", true);
        model.addAttribute("message", "Инструкции по сбросу пароля отправлены на почту: " + resetRequest.getEmail());

        return "fragments/reset-password :: reset-password-fragment";
    }

    @GetMapping("/reset-password/validate")
    public String showResetForm(@RequestParam("token") String token, Model model) {

        if(!resetService.validateToken(token)) {
            model.addAttribute("error", "The reset link is invalid or has expired.");
            return "auth/reset-password";
        }

        model.addAttribute("token", token);
        model.addAttribute("validToken", true);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password/confirm")
    public String confirmReset(@ModelAttribute @Valid ResetPasswordRequest request, HttpServletResponse response) {

        if(!resetService.validateToken(request.getToken())) {
            return "redirect:/auth/sign-in?error=invalid_token";
        }

        authenticationService.resetPassword(request);
        resetCookies(response);

        return "redirect:/auth/sign-in?resetSuccess=true";
    }

    private void saveTokensToCookies(HttpServletResponse response, String access, String refresh) {
        Cookie aCookie = new Cookie("jwt_access", access);
        aCookie.setHttpOnly(true);
        aCookie.setPath("/");
        aCookie.setMaxAge((int) jwtProperties.getExpiration());

        Cookie rCookie = new Cookie("jwt_refresh", refresh);
        rCookie.setHttpOnly(true);
        rCookie.setPath("/auth/refresh");
        rCookie.setMaxAge((int) jwtProperties.getRefreshToken().getExpiration());

        response.addCookie(aCookie);
        response.addCookie(rCookie);
    }

    private void resetCookies(HttpServletResponse response) {
        Cookie aCookie = new Cookie("jwt_access", null);
        aCookie.setPath("/");
        aCookie.setMaxAge(0);

        Cookie rCookie = new Cookie("jwt_refresh", null);
        rCookie.setPath("/");
        rCookie.setMaxAge(0);

        response.addCookie(aCookie);
        response.addCookie(rCookie);
    }
}
