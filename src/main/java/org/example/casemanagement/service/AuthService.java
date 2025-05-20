package org.example.casemanagement.service;

import lombok.RequiredArgsConstructor;
import org.example.casemanagement.dto.AuthResponse;
import org.example.casemanagement.dto.LoginRequest;
import org.example.casemanagement.dto.RegisterRequest;
import org.example.casemanagement.model.entity.User;
import org.example.casemanagement.repository.UserRepository;
import org.example.casemanagement.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        
        var jwt = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверное имя пользователя или пароль");
        }

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        var jwt = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
} 