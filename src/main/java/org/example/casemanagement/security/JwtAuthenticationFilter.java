package org.example.casemanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            log.debug("Processing request for URI: {}", request.getRequestURI());
            String jwt = extractJwtToken(request);
            
            if (jwt != null) {
                log.debug("Found JWT token, attempting to process");
                processJwtToken(jwt, request);
            } else {
                log.debug("No JWT token found for request");
            }
            
            log.debug("Current authentication: {}", SecurityContextHolder.getContext().getAuthentication());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error in JWT filter", e);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }

    private String extractJwtToken(HttpServletRequest request) {
        // First try to get token from cookie
        if (request.getCookies() != null) {
            String cookieToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "Authorization".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

            if (cookieToken != null) {
                log.debug("Found JWT token in cookie");
                return cookieToken;
            }
        }

        // If not in cookie, try header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Found JWT token in Authorization header");
            return authHeader.substring(7);
        }

        log.debug("No JWT token found for request: {}", request.getRequestURI());
        return null;
    }

    private void processJwtToken(String jwt, HttpServletRequest request) {
        try {
            String username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Successfully authenticated user: {}", username);
                } else {
                    log.debug("JWT token is invalid for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            SecurityContextHolder.clearContext();
        }
    }
} 