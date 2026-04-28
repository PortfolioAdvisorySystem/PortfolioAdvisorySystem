package com.backend.stockAllocation.controller;

import com.backend.stockAllocation.dto.request.LoginDto;
import com.backend.stockAllocation.entity.Subscriber;
import com.backend.stockAllocation.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<Optional<Subscriber>> login(@RequestBody LoginDto loginDto)
    {
        return ResponseEntity.ok(authService.login(loginDto));
    }
}
