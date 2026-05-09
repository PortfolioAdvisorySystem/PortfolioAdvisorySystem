package com.backend.stockAllocation.controller;

import com.backend.stockAllocation.authentication.AuthDTOs;
import com.backend.stockAllocation.authentication.AuthService;
import com.backend.stockAllocation.entity.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDTOs.AuthResponse register(
            @Valid @RequestBody AuthDTOs.RegisterRequest request) {
        return authService.register(request);
    }
    @PostMapping("/login")
    public AuthDTOs.AuthResponse login(
            @Valid @RequestBody AuthDTOs.LoginRequest request) {
        return authService.login(request);
    }
    @PostMapping("/refresh")
    public AuthDTOs.AuthResponse refresh(
            @Valid @RequestBody AuthDTOs.RefreshRequest request) {
        return authService.refreshToken(request);
    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getSubscriberId()
        ));
    }
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody AuthDTOs.ChangePasswordRequest request) {
        authService.changePassword(user.getId(), request);
    }
    public record UserProfileResponse(
            Long   userId,
            String email,
            String firstName,
            String lastName,
            String role,
            Long   subscriberId
    ) {}
}
