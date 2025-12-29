package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

	private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(UUID.fromString(id)));
    }

    @PatchMapping("/update/{username}")
    public ResponseEntity<Void> updateUserById(@PathVariable String username, @RequestBody UserDTO userDTO) {
        userService.updateUser(username, userDTO);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-account-status/")
    public ResponseEntity<Void> changeAccountStatus(@RequestBody UserDTO userDTO) {
        userService.changeAccountStatus(userDTO);

        return ResponseEntity.ok().build();
    }
}
