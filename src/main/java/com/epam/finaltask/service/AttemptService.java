package com.epam.finaltask.service;

public interface AttemptService {

    void track(String ip);

    boolean isBlocked(String ip);

    void clearBlocked(String ip);
}
