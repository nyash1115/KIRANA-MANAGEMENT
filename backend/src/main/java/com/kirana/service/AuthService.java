package com.kirana.service;

import com.kirana.dto.AuthRequest;
import com.kirana.dto.AuthResponse;
import com.kirana.dto.ChangePasswordRequest;
import com.kirana.entity.User;
import com.kirana.exception.InvalidOperationException;
import com.kirana.exception.ResourceNotFoundException;
import com.kirana.repository.UserRepository;
import com.kirana.security.JwtTokenProvider;
import com.kirana.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        auditService.logAction(user.getBusiness(), user.getStore(), user, "LOGIN", "User", String.valueOf(user.getId()), null, "User logged in", null);

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStore() != null ? user.getStore().getId() : null,
                user.getBusiness() != null ? user.getBusiness().getId() : null
        );
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new InvalidOperationException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.logAction(user.getBusiness(), user.getStore(), user, "CHANGE_PASSWORD", "User", String.valueOf(user.getId()), null, "Password changed", null);
    }
}
