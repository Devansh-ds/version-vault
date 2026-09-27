package com.version_vault.controller;

import com.version_vault.response.UserResponse;
import com.version_vault.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getUserById(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(userService.findByJwtTokenTest(token), HttpStatus.OK);
    }

    @GetMapping("/e")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return new ResponseEntity<>(userService.getUserByEmail(email), HttpStatus.OK);
    }

    @GetMapping("/u")
    public ResponseEntity<UserResponse> getUserByUsername(@RequestParam String username) {
        return new ResponseEntity<>(userService.getUserByUsername(username), HttpStatus.OK);
    }

}
