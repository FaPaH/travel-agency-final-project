package com.epam.finaltask.service;

import com.epam.finaltask.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("auth")
public class SecurityService {

    public boolean isUserObject(String userId) {
        UUID uuid = UUID.fromString(userId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return false;
        }
        return user.getId().equals(uuid);
    }
}
