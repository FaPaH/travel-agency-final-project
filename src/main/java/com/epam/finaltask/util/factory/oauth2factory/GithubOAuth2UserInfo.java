package com.epam.finaltask.util.factory.oauth2factory;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getUsername() {
        String login = (String) attributes.get("login");
        String email = (String) attributes.get("email");

        if (login != null && !login.isEmpty()) {
            return login;
        }

        if (email != null && !email.isEmpty()) {
            return email;
        }

        return null;
    }

    @Override
    public String getFirstName() {
        String fullName = getName();
        return splitName(fullName)[0];
    }

    @Override
    public String getLastName() {
        String fullName = getName();
        return splitName(fullName)[1];
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return new String[]{null, null};
        }
        String[] parts = fullName.trim().split(" ", 2);
        return new String[]{
                parts[0],
                parts.length > 1 ? parts[1] : ""
        };
    }
}
