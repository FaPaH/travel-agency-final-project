package com.epam.finaltask.util.factory.oauth2factory;

import java.util.Map;

public class FacebookOAuth2UserInfo extends OAuth2UserInfo {

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
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
        return getEmail();
    }

    @Override
    public String getFirstName() {
        if (attributes.containsKey("first_name")) {
            return (String) attributes.get("first_name");
        }

        return splitName((String) attributes.get("name"))[0];
    }

    @Override
    public String getLastName() {
        if (attributes.containsKey("last_name")) {
            return (String) attributes.get("last_name");
        }

        return splitName((String) attributes.get("name"))[1];
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return new String[]{null, null};
        }
        String[] parts = fullName.split(" ", 2);
        return new String[]{
                parts[0],
                parts.length > 1 ? parts[1] : ""
        };
    }
}
