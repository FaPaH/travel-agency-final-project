package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.TopUpRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserRestController {

	private final UserService userService;

    @GetMapping("/profile/{id}")
    @PreAuthorize("@auth.isUserObject(#id)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(UUID.fromString(id)));
    }

    @PostMapping("/profile/{id}/update")
    @PreAuthorize("@auth.isUserObject(#id)")
    public ResponseEntity<UserDTO> updateUserById(@PathVariable String id,
                                                  @RequestBody @Valid UserDTO userDTO) {

        return ResponseEntity.ok().body(userService.updateUser(userDTO.getUsername(), userDTO));
    }

    @PostMapping("/profile/{id}/balance/top-up")
    @PreAuthorize("@auth.isUserObject(#id)")
    public ResponseEntity<UserDTO> updateBalance(@PathVariable String id,
                                                 @RequestBody @Valid TopUpRequest topUpRequest) {

        return ResponseEntity.ok().body(userService.changeBalance(id, topUpRequest.getAmount()));
    }
}
