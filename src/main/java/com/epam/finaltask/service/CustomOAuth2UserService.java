package com.epam.finaltask.service;

import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email = oAuth2User.getAttribute("email");
        String login = oAuth2User.getAttribute("login");
        User user;

        if (email == null) {
            if ("github".equalsIgnoreCase(registrationId) || "facebook".equalsIgnoreCase(registrationId)) {
                user = userRepository.findUserByUsername(login).orElse(null);
            } else {
                throw new RuntimeException("Email not found");
            }
        } else {
            user = userRepository.findUserByEmail(email).orElse(null);
        }

        if (user == null) {
            user = registerNewUser(userRequest, oAuth2User, email, login);
        } else {
            user = updateExistingUser(user, userRequest, oAuth2User);
        }

        return oAuth2User;
    }

    private User updateExistingUser(User user, OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // TODO: update logic?
        return userRepository.save(user);
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2User oAuth2User, String email, String login) {
        User user = User.builder()
                .authProvider(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase()))
                .username(login == null ? email.replaceAll("@.*", "") : login)
                .email(email)
                .role(Role.USER)
                .active(true)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        return userRepository.save(user);
    }
}
