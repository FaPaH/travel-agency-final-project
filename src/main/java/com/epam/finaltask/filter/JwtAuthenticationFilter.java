package com.epam.finaltask.filter;

import com.epam.finaltask.dto.ErrorResponse;
import com.epam.finaltask.exception.InvalidTokenException;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final static String BEARER_PREFIX = "Bearer ";
    private final static String HEADER_NAME = "Authorization";


    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestPath = request.getServletPath();

        try {
            String authHeader = request.getHeader(HEADER_NAME);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                if (requestPath.contains("/api/auth") ||
                        requestPath.contains("/swagger-ui") ||
                        requestPath.contains("/v3/api-docs")) {

                    filterChain.doFilter(request, response);
                    return;
                }

                handleErrorResponse(response, HttpStatus.UNAUTHORIZED, "JWT Token is missing or invalid format", requestPath);
                return;
            }

            String jwt = authHeader.substring(BEARER_PREFIX.length());
            String username = jwtUtil.extractUsername(jwt);

                if (!StringUtils.isEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userService
                            .userDetailsService()
                            .loadUserByUsername(username);

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
                    }
                }
                filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token is expired", requestPath);
        } catch (JwtException e) {
            handleErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token", requestPath);
        } catch (Exception e) {
            handleErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication error", requestPath);
        }
    }

    private void handleErrorResponse(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(status.value())
                .message(message)
                .error(status.getReasonPhrase())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
