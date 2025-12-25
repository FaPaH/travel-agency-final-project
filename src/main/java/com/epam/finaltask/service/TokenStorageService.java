package com.epam.finaltask.service;

public interface TokenStorageService<T> {

    void store(String id, T token);

    T get(String id);

    void revoke(String id);
}
