package com.backend.stockAllocation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDto {

    @NotNull(message = "email is required")
    @Email(message = "enter valid email")
    private String email;
    @NotNull(message = "password is required")
    private String password;
}
