package com.trading.service;

import com.trading.dto.AuthRequest;
import com.trading.dto.AuthResponse;
import com.trading.dto.RegisterRequest;
import com.trading.entity.User;
import com.trading.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return new AuthResponse(false, "Username already exists. Please choose a different username.");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse(false, "Email address is already registered. Please use a different email.");
            }
            
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setCreatedAt(LocalDateTime.now());
            user.setActive(true);
            user.setRole("TRADER");
            
            // Save user
            User savedUser = userRepository.save(user);
            
            // Generate token
            String token = generateToken(savedUser);
            
            return new AuthResponse(true, "Account created successfully!", token, savedUser);
            
        } catch (Exception e) {
            return new AuthResponse(false, "Registration failed: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user login
     */
    public AuthResponse signin(AuthRequest request) {
        try {
            // Find user by username or email
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(request.getUsername());
            
            if (userOpt.isEmpty()) {
                return new AuthResponse(false, "Invalid username or password.");
            }
            
            User user = userOpt.get();
            
            // Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return new AuthResponse(false, "Invalid username or password.");
            }
            
            // Update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate token
            String token = generateToken(user);
            
            return new AuthResponse(true, "Sign in successful!", token, user);
            
        } catch (Exception e) {
            return new AuthResponse(false, "Sign in failed: " + e.getMessage());
        }
    }
    
    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findActiveUserByUsername(username);
    }
    
    /**
     * Validate token and get user
     */
    public Optional<User> validateToken(String token) {
        try {
            // Simple token validation - in production, use JWT
            if (token != null && token.startsWith("TT_")) {
                String username = extractUsernameFromToken(token);
                return getUserByUsername(username);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Generate simple token (in production, use JWT)
     */
    private String generateToken(User user) {
        return "TT_" + user.getUsername() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Extract username from token
     */
    private String extractUsernameFromToken(String token) {
        if (token.startsWith("TT_")) {
            String[] parts = token.split("_");
            if (parts.length >= 3) {
                return parts[1];
            }
        }
        throw new IllegalArgumentException("Invalid token format");
    }
}
