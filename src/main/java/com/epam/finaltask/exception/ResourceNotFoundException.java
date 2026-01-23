package com.epam.finaltask.exception;

public class ResourceNotFoundException extends LocalizedException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super("error.resource.not_found", resourceName, id);
    }
}
