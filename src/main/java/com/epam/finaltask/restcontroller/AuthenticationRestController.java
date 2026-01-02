package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.model.ResetToken;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.ResetService;
import com.epam.finaltask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;
    private final ResetService resetService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok().body(authenticationService.register(registerRequest));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok().body(authenticationService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok().body(authenticationService.refresh(refreshTokenRequest));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest logoutRequest) {
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
    public ResponseEntity<String> requestReset(@RequestBody ResetRequest request) {
        resetService.proceedReset(request.getEmail());
        return ResponseEntity.ok("If the email is registered, you'll get a reset link");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        System.out.println("token: " + token);
        if(!resetService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }

        return ResponseEntity.ok("Token is valid");
    }

    @PostMapping("/reset-password/confirm")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        if(!resetService.validateToken(request.getToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is invalid or expired");
        }

        ResetToken tokenRecord = resetService.getResetToken(request.getToken());
        UserDTO user = tokenRecord.getUserDTO();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateUser(user.getUsername(), user);
        resetService.removeResetToken(tokenRecord.getToken());

        return ResponseEntity.ok("Password updated");
    }
}
