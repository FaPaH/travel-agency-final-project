package com.epam.finaltask.model;

import com.epam.finaltask.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetToken {

    private String token;

    private LocalDateTime expiresAt;

    private UserDTO userDTO;

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
