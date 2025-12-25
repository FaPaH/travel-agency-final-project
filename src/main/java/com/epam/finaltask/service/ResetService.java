package com.epam.finaltask.service;

import com.epam.finaltask.model.ResetToken;

public interface ResetService {

    void proceedReset(String email);

    boolean validateToken(String token);

    ResetToken getResetToken(String token);

    void removeResetToken(String token);
}
