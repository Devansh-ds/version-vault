package com.version_vault.service;

import com.version_vault.exceptions.ResourceAlreadyExistsException;
import com.version_vault.exceptions.ResourceNotFoundException;
import com.version_vault.mapper.UserMapper;
import com.version_vault.models.User;
import com.version_vault.repo.UserRepository;
import com.version_vault.request.CreateUserRequest;
import com.version_vault.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists: "  + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists: "  + request.getEmail());
        }

        User user = new User(request.getUsername(), request.getEmail());
        User savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

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

}
