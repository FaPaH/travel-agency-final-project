package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.ResetToken;
import com.epam.finaltask.util.JwtProperties;
import com.epam.finaltask.util.ResetTokenUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResetServiceImpl implements ResetService {

    private final ResetTokenUtil resetTokenUtil;
    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final TokenStorageService<ResetToken> resetTokenStorageService;
    private final MailService mailService;

    private final static String RESET_API_URL = "http://localhost:8080/api/auth/reset-password/validate?token=";
    private final static String RESET_URL = "http://localhost:8080/auth/reset-password/validate?token=";
    private final static String RESET_BODY = "Click the link below to reset your password.\n";

    @Override
    public void proceedReset(String email, boolean isApi) {
        UserDTO userDTO = userService.getUserByEmail(email);

        String token = resetTokenUtil.generateResetToken();

        ResetToken resetToken = ResetToken.builder()
                .token(token)
                .userDTO(userDTO)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getExpiration()))
                .build();

        resetTokenStorageService.store(token, resetToken);

        String resetUrl;

        if (isApi) {
            resetUrl = RESET_API_URL + token;
        } else {
            resetUrl = RESET_URL + token;
        }

        String body = RESET_BODY + resetUrl;

        mailService.sendTextMail(userDTO.getEmail(), resetToken.getToken(), body);
    }

    @Override
    public boolean validateToken(String token) {
        return resetTokenStorageService.get(token) != null && !resetTokenStorageService.get(token).isExpired();
    }

    @Override
    public ResetToken getResetToken(String token) {
        return resetTokenStorageService.get(token);
    }

    @Override
    public void removeResetToken(String token) {
        resetTokenStorageService.revoke(token);
    }
}
