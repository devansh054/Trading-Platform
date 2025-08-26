package com.trading.controller;

import com.trading.dto.AuthRequest;
import com.trading.dto.AuthResponse;
import com.trading.dto.RegisterRequest;
import com.trading.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * User registration endpoint
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            System.out.println("Received signup request: " + request.getUsername() + ", " + request.getEmail() + ", " + request.getFullName());
            AuthResponse response = authService.register(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            AuthResponse errorResponse = new AuthResponse(false, "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * User sign in endpoint
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.signin(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse(false, "Sign in failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Validate token endpoint
     */
    @PostMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String token) {
        try {
            var user = authService.validateToken(token.replace("Bearer ", ""));
            
            if (user.isPresent()) {
                AuthResponse response = new AuthResponse(true, "Token is valid", token, user.get());
                return ResponseEntity.ok(response);
            } else {
                AuthResponse response = new AuthResponse(false, "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse(false, "Token validation failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Sign out endpoint
     */
    @PostMapping("/signout")
    public ResponseEntity<AuthResponse> signout() {
        // In a real application, you would invalidate the token here
        AuthResponse response = new AuthResponse(true, "Signed out successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create test user endpoint for demo
     */
    @PostMapping("/create-test-user")
    public ResponseEntity<AuthResponse> createTestUser() {
        try {
            RegisterRequest testRequest = new RegisterRequest();
            testRequest.setUsername("testuser");
            testRequest.setPassword("password");
            testRequest.setEmail("test@example.com");
            testRequest.setFullName("Test User");
            
            AuthResponse response = authService.register(testRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse(false, "Failed to create test user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
