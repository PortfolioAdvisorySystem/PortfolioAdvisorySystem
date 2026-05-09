package com.backend.stockAllocation.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTOs {
    @Data
    public static class RegisterRequest {

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50)
        private String lastName;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
        // Leave null for ADMIN/ANALYST registrations
        private Long subscriberId;

        private String role;
    }

    @Data
    public static class LoginRequest {

        @Email
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }

    @Data
    @lombok.Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;           // "Bearer"
        private long   expiresIn;
        private Long   userId;
        private String email;
        private String firstName;
        private String role;
        private Long   subscriberId;        // null for ADMIN
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        @Size(min = 8)
        private String newPassword;
    }
}
