package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private AppUserService userService;

    /**
     * Login Endpoint - gibt Feedback bei erfolgreicher Authentifizierung
     * POST /login mit Authorization: Basic <base64(username:password)>
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication authentication) {
        String username = authentication.getName();

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "username", username
        ));
    }

    /**
     * Registration Endpoint
     * POST /register mit Body: {"username": "...", "password": "..."}
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password must be at least 6 characters"));
            }

            AppUser user = userService.registerUser(username, password);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "User registered successfully",
                    "username", user.getUsername()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}