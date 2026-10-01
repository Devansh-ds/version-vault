package com.version_vault.security.service;

import com.version_vault.exceptions.ResourceAlreadyExistsException;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.models.User;
import com.version_vault.repo.UserRepository;
import com.version_vault.security.dto.AuthenticationRequest;
import com.version_vault.security.dto.AuthenticationResponse;
import com.version_vault.security.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) throws ResourceAlreadyExistsException {

        if ("admin".equalsIgnoreCase(request.getFullname().trim()) ||
                "admin@gmail.com".equalsIgnoreCase(request.getEmail().trim())) {
            throw new ResourceAlreadyExistsException("Username/Email already in use");
        }

        var user = new User(
                request.getFullname().trim(),
                request.getEmail().trim(),
                passwordEncoder.encode(request.getPassword())
        );

        var refreshToken = jwtService.generateRefreshToken(user);

        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
            userRepository.findByUsername(request.getFullname()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email/Username already in use");
        }

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws ResourceNotFoundException {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim(),
                        request.getPassword().trim()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(request.getEmail() + " does not exist"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }
}
