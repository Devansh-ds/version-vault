package com.version_vault.service;

import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.exceptions.UnauthorizedException;
import com.version_vault.mapper.UserMapper;
import com.version_vault.models.User;
import com.version_vault.repo.UserRepository;
import com.version_vault.dtos.response.UserResponse;
import com.version_vault.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toUserResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->  new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    public User getOriginalUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByJwtToken(String token) throws ResourceNotFoundException {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException("User not found with email: " + email));
        return user;
    }

    public UserResponse findByJwtTokenTest(String token) throws ResourceNotFoundException {
        token = token.substring(7);
        String email = jwtService.extractUsername(token);
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    public void validateSystemAdmin(User user) {
        if (!"admin".equalsIgnoreCase(user.getOriginalUsername()) || !"admin@gmail.com".equalsIgnoreCase(user.getEmail())) {
            throw new UnauthorizedException(
                    "Only the system administrator can perform this operation"
            );
        }
    }

}
