package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.ResetService;
import com.epam.finaltask.util.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/sign-in")
    public String signIn(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());

        return "auth/sign-in";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());

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

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok().body(authenticationService.refresh(refreshTokenRequest));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest logoutRequest) {
        authenticationService.logout(logoutRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(
            @RequestParam("accessToken") String accessToken,
            @RequestParam("refreshToken") String refreshToken
    ) {
        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build()
        );
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> requestReset(@RequestBody @Valid ResetRequest request) {
        resetService.proceedReset(request.getEmail());
        return ResponseEntity.ok("If the email is registered, you'll get a reset link");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        if(resetService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }

        return ResponseEntity.ok("Token is valid");
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request);

        return ResponseEntity.ok("Password updated");
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
}
