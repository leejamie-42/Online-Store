package com.comp5348.store.security;

import com.comp5348.store.service.auth.TokenBlacklistService;
import com.comp5348.store.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

/**
 * JWT Authentication Filter
 *
 * Intercepts incoming requests, extracts JWT token from Authorization header,
 * validates the token against blacklist, and sets the authentication in the SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final TokenBlacklistService tokenBlacklistService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    // Get Authorization header
    final String authHeader = request.getHeader("Authorization");

    // Skip if no Authorization header or doesn't start with "Bearer "
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Extract JWT token
      final String jwt = authHeader.substring(7);

      // Check if token is blacklisted (logged out)
      if (tokenBlacklistService.isBlacklisted(jwt)) {
        log.warn("Attempted to use blacklisted (logged out) token");
        filterChain.doFilter(request, response);
        return;
      }

      // Extract username from token
      final String username = jwtUtil.extractUsername(jwt);

      // Authenticate if username exists and no authentication in context
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Validate token
        if (jwtUtil.validateToken(jwt, userDetails)) {
          // Create authentication token
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());

          // Set authentication details
          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));

          // Set authentication in SecurityContext
          SecurityContextHolder.getContext().setAuthentication(authToken);

          log.debug("JWT authentication successful for user: {}", username);
        } else {
          log.warn("JWT validation failed for user: {}", username);
        }
      }
    } catch (Exception e) {
      log.error("JWT authentication error: {}", e.getMessage());
      // Continue filter chain even on error (authentication will remain null)
    }

    // Continue filter chain
    filterChain.doFilter(request, response);
  }
}
