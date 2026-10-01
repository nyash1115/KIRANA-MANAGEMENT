package com.kirana.controller;

import com.kirana.dto.ApiResponse;
import com.kirana.dto.AuthRequest;
import com.kirana.dto.AuthResponse;
import com.kirana.dto.ChangePasswordRequest;
import com.kirana.entity.User;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.UserRepository;
import com.kirana.security.UserPrincipal;
import com.kirana.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, profile, and password management")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with username and password, returns JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("Not authenticated");
        }
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AuthResponse resp = new AuthResponse(
                null,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStore() != null ? user.getStore().getId() : null,
                user.getBusiness() != null ? user.getBusiness().getId() : null
        );
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change current user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
