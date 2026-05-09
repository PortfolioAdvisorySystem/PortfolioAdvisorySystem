package com.backend.stockAllocation.authentication;

import com.backend.stockAllocation.entity.AppUser;
import com.backend.stockAllocation.enums.Role;
import com.backend.stockAllocation.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService          jwtService;
    private final AuthenticationManager authenticationManager;
    private static final long JWT_EXPIRY_SECONDS = 86400L; // 24h

    @Transactional
    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already registered: " + request.getEmail());
        }
        Role role = Role.SUBSCRIBER;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
        }
        AppUser user = AppUser.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .subscriberId(request.getSubscriberId())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        AppUser saved = userRepository.save(user);
        log.info("[AUTH] Registered new user: {} with role: {}", saved.getEmail(), saved.getRole());
        return buildAuthResponse(saved);
    }
    @Transactional
    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("[AUTH] Failed login attempt for: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        AppUser user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        log.info("[AUTH] Successful login: {}", user.getEmail());
        return buildAuthResponse(user);
    }


    @Transactional(readOnly = true)
    public AuthDTOs.AuthResponse refreshToken(AuthDTOs.RefreshRequest request) {

        final String refreshToken = request.getRefreshToken();
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadCredentialsException("Refresh token expired or invalid");
        }
        String newAccessToken = jwtService.generateToken(user);

        return AuthDTOs.AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)    // same refresh token
                .tokenType("Bearer")
                .expiresIn(JWT_EXPIRY_SECONDS)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole().name())
                .subscriberId(user.getSubscriberId())
                .build();
    }


    @Transactional
    public void changePassword(Long userId, AuthDTOs.ChangePasswordRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("[AUTH] Password changed for user: {}", user.getEmail());
    }

  //helper method to build auth response
    private AuthDTOs.AuthResponse buildAuthResponse(AppUser user) {
        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthDTOs.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(JWT_EXPIRY_SECONDS)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole().name())
                .subscriberId(user.getSubscriberId())
                .build();
    }
}
