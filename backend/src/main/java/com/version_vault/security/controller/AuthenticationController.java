package com.version_vault.security.controller;

import com.version_vault.exceptions.ResourceAlreadyExistsException;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.response.UserResponse;
import com.version_vault.security.dto.AuthenticationRequest;
import com.version_vault.security.dto.AuthenticationResponse;
import com.version_vault.security.dto.RegisterRequest;
import com.version_vault.security.service.AuthenticationService;
import com.version_vault.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) throws ResourceAlreadyExistsException {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request) throws ResourceNotFoundException {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUserByJwt(@RequestHeader("Authorization") String token) throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.findByJwtTokenTest(token));
    }
}
