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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            @Valid @ModelAttribute RegisterRequest request,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return "redirect:/register?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
        }
        try {
            var authResponse = authService.register(request);
            setAuthCookie(authResponse.getToken(), response);
            return "redirect:/boards";
        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.getUsername(), e);
            String errorMsg = e.getMessage();
            return "redirect:/register?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
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
            String errorMsg = "Неверное имя пользователя или пароль";
            return "redirect:/login?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
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