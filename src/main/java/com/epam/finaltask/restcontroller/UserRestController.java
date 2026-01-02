package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

	private final UserService userService;

    //TODO: Add security annotations

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<UserDTO> getUserById(@AuthenticationPrincipal User user, @PathVariable String id) {

        if (!Objects.equals(user.getId().toString(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(UUID.fromString(id)));
    }

    @PatchMapping("/update/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> updateUserById(@AuthenticationPrincipal User user,
                                               @PathVariable String username,
                                               @RequestBody UserDTO userDTO) {

        if (!Objects.equals(user.getId().toString(), userDTO.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        userService.updateUser(username, userDTO);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-account-status/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeAccountStatus(@RequestBody UserDTO userDTO) {
        userService.changeAccountStatus(userDTO);

        return ResponseEntity.ok().build();
    }
}
