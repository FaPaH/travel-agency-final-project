package com.epam.finaltask.util.factory.oauth2factory;

import java.util.Map;

public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getUsername();

    public abstract String getFirstName();

    public abstract String getLastName();
}
