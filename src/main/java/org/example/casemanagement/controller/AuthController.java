package org.example.casemanagement.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.casemanagement.dto.LoginRequest;
import org.example.casemanagement.dto.RegisterRequest;
import org.example.casemanagement.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private void setAuthCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", token);
        cookie.setHttpOnly(true);
        // Set secure to false for local development
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1 day
        response.addCookie(cookie);
    }

    @PostMapping("/register")
    public String register(
            @Valid RegisterRequest request,
            HttpServletResponse response
    ) {
        try {
            var authResponse = authService.register(request);
            setAuthCookie(authResponse.getToken(), response);
            return "redirect:/boards";
        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.getUsername(), e);
            return "redirect:/register?error=" + e.getMessage();
        }
    }

    @PostMapping("/login")
    public String login(
            @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        try {
            var authResponse = authService.login(request);
            setAuthCookie(authResponse.getToken(), response);
            return "redirect:/boards";
        } catch (Exception e) {
            log.error("Login failed for username: {}", request.getUsername(), e);
            return "redirect:/login?error=" + e.getMessage();
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
} 