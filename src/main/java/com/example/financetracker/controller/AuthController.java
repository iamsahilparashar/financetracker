package com.example.financetracker.controller;

import com.example.financetracker.config.JwtUtil;
import com.example.financetracker.entity.User;
import com.example.financetracker.repository.UserRepository;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request) {

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .isApplicationUser(true)
                .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId());

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getId());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // inline DTOs — simple enough to keep here
    @Getter @Setter
    static class RegisterRequest {
        private String name;
        private String email;
        private String phone;
    }

    @Getter @Setter
    static class LoginRequest {
        private String phone;
    }

    @Getter @AllArgsConstructor
    static class AuthResponse {
        private String token;
    }
}