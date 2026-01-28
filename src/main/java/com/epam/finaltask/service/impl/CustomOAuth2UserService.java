package com.epam.finaltask.service.impl;

import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.UserPrincipal;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.util.factory.oauth2factory.OAuth2UserInfo;
import com.epam.finaltask.util.factory.oauth2factory.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException("Failed to process OAuth2User", ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        User user = userRepository.findUserByEmail(userInfo.getEmail()).orElse(null);

        if (user != null) {
            if (!user.getAuthProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
                throw new OAuth2AuthenticationException(new OAuth2Error("error.auth.wrong_provider"),
                        "Wrong provider used");
            }
            user = updateExistingUser(user, userInfo);
        } else {
            user = registerNewUser(userRequest, userInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo) {
        existingUser.setUsername(userInfo.getUsername());

        if (userInfo.getFirstName() != null) {
            existingUser.setFirstName(userInfo.getFirstName());
        }
        if (userInfo.getLastName() != null) {
            existingUser.setLastName(userInfo.getLastName());
        }

        return userRepository.save(existingUser);
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo) {
        String usernameCandidate = userInfo.getUsername();

        if (usernameCandidate == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("error.auth.email_not_found"),
                    "Email not found");
        }

        User user = User.builder()
                .authProvider(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase()))
                .username(usernameCandidate)
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .role(Role.USER)
                .active(true)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        return userRepository.save(user);
    }
}
