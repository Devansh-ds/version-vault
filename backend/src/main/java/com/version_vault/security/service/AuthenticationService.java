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

        var user = new User(
                request.getFullname(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        var refreshToken = jwtService.generateRefreshToken(user);

        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
            userRepository.findByUsername(request.getFullname()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email/Username already in use");
        }

        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }

//    private void revokeAllUserTokens(User user) {
//        var validToken = tokenRepository.findAllValidTokensByUser(user.getId());
//        if (validToken.isEmpty()) {
//            return;
//        }
//        validToken.forEach(token -> {
//            token.setExpired(true);
//            token.setRevoked(true);
//        });
//        tokenRepository.saveAll(validToken);
//    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) throws ResourceNotFoundException {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(request.getEmail() + " does not exist"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

//        revokeAllUserTokens(user);
//        saveUserToken(user, jwtToken);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }

//    private void saveUserToken(User savedUser, String jwtToken) {
//        var token = new Token(
//                jwtToken,
//                TokenType.BEARER,
//                false,
//                false,
//                savedUser
//        );
//        tokenRepository.save(token);
//    }

//    public AuthenticationResponse refreshToken(String refreshToken) throws TokenInvalidException {
//
//        String userEmail;
//
//        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
//            throw new TokenInvalidException("token might be null or empty");
//        }
//
//        refreshToken = refreshToken.substring(7);
//        userEmail = jwtService.extractUsername(refreshToken);
//
//        if (userEmail != null) {
//            var user = userRepository.findByEmail(userEmail)
//                    .orElseThrow(() -> new UsernameNotFoundException(userEmail));
//
//            if (jwtService.validateToken(refreshToken, user)) {
//                var accessToken = jwtService.generateToken(user);
//                revokeAllUserTokens(user);
//                saveUserToken(user, accessToken);
//
//                return new AuthenticationResponse(accessToken, refreshToken);
//            } else {
//                throw new TokenInvalidException("refresh token is invalid");
//            }
//        } else {
//            throw new TokenInvalidException("Weird refresh token");
//        }
//    }
}
