package com.backend.stockAllocation.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService         jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
         String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            log.warn("[JWT] Failed to extract username from token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Only authenticate if not already authenticated in this request
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Create authentication token with authorities (roles)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // no credentials needed post-auth
                                userDetails.getAuthorities()
                        );

                // Attach request details (IP address, session ID) to the auth token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store in SecurityContext — this is what @PreAuthorize reads
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("[JWT] Authenticated user: {}, roles: {}",
                        userEmail, userDetails.getAuthorities());
            } else {
                log.warn("[JWT] Token invalid for user: {}", userEmail);
            }
        }

        filterChain.doFilter(request, response);
    }
}
