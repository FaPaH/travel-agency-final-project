package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {
    UserDTO saveUser(UserDTO userDTO, String password);

    UserDTO updateUser(String username, UserDTO userDTO);
    UserDTO changeBalance(String userId, BigDecimal amount);

    UserDTO getUserByUsername(String username);
    UserDTO changeAccountStatus(UserDTO userDTO);
    UserDTO getUserById(UUID id);

    UserDetailsService userDetailsService();

    UserDTO getUserByEmail(String email);

    void changePassword(UserDTO userDTO, String newPassword);

    PaginatedResponse<UserDTO> getAllUsers(Pageable pageable);
}
