package com.epam.finaltask.filter;

import com.epam.finaltask.exception.InvalidTokenException;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final static String BEARER_PREFIX = "Bearer ";
    private final static String HEADER_NAME = "Authorization";

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("Starting JWT Authentication Filter");

        String requestPath = request.getServletPath();
        final String authHeader = request.getHeader(HEADER_NAME);
        final String jwt;
        final String username;

        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                if (requestPath.contains("/api/auth") ||
                        requestPath.contains("/swagger-ui") ||
                        requestPath.contains("/v3/api-docs")) {

                    filterChain.doFilter(request, response);
                    return;
                }

                throw new InvalidTokenException("JWT Authentication Filter Skipped");
            }

            jwt = authHeader.substring(BEARER_PREFIX.length());
            username = jwtUtil.extractUsername(jwt);

            if (!StringUtils.isEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService
                        .userDetailsService()
                        .loadUserByUsername(username);

                log.info("Logged in user: {}", userDetails);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    SecurityContext context = SecurityContextHolder.getContext();

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                } else {
                    throw new InvalidTokenException("JWT Authentication Filter Skipped");
                }
            } else {
                throw new InvalidTokenException("JWT Authentication Filter Skipped");
            }

            log.info("JWT Authentication Filter Success for username {}", username);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }
    }
}
