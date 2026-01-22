package com.epam.finaltask.config;

import com.epam.finaltask.filter.JwtAuthenticationFilter;
import com.epam.finaltask.filter.LoginAttemptFilter;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.*;
import com.epam.finaltask.service.impl.CustomOAuth2UserService;
import com.epam.finaltask.util.CustomOAuth2FailureHandler;
import com.epam.finaltask.util.HtmxAuthenticationEntryPoint;
import com.epam.finaltask.util.JwtProperties;
import com.epam.finaltask.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginAttemptFilter loginAttemptFilter;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomOAuth2UserService oAuth2UserService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final TokenStorageService<String> refreshTokenStorageService;
    private final JwtProperties jwtProperties;
    private final HtmxAuthenticationEntryPoint htmxAuthenticationEntryPoint;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/auth/**", "/auth/**", "error/error").permitAll()
                        .requestMatchers("/favicon.ico","/","/index", "/css/**", "/js/**", "/vouchers").permitAll()
                        .requestMatchers("/api/auth/reset-password", "/auth/reset-password", "/user/dashboard", "user/profile/**").authenticated()
                        .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginAttemptFilter, JwtAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/auth/sign-in")
                        .loginProcessingUrl("/auth/login-security-check")
                        .defaultSuccessUrl("/index", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler())
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .deleteCookies("JSESSIONID", "jwt_access", "jwt_refresh")
                        .invalidateHttpSession(true))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .failureHandler(customOAuth2FailureHandler)
                        .loginPage("/auth/sign-in")
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                ).exceptionHandling(exception -> exception
                        .authenticationEntryPoint(htmxAuthenticationEntryPoint)
                );

        return http.build();
    }

    @Bean
    LogoutHandler logoutHandler() {
        return new LogoutHandler() {

            @Override
            public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                String token = null;

                if (request.getCookies() != null) {
                    token = Arrays.stream(request.getCookies())
                            .filter(c -> "jwt_access".equals(c.getName()))
                            .map(Cookie::getValue)
                            .findFirst()
                            .orElse(null);
                }

                if (token != null) {
                    refreshTokenStorageService.revoke(jwtUtil.extractClaim(token, claims -> claims.get("id", String.class)));
                }
            }
        };
    }

    @Bean
    LogoutSuccessHandler logoutSuccessHandler() {
        return new LogoutSuccessHandler() {

            @Override
            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                        request.getHeader("HX-Request") != null) {

                    response.setHeader("HX-Redirect", "/auth/sign-in?logout");
                } else {
                    response.sendRedirect("/auth/sign-in?logout");
                }
            }
        };
    }

    @Bean
    AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication)
                    throws IOException, ServletException {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

                String email = oAuth2User.getAttribute("email");
                String username = oAuth2User.getAttribute("login");
                User user;
                String clientType = request.getParameter("client_type");

                DefaultSavedRequest savedRequest = (DefaultSavedRequest) new HttpSessionRequestCache()
                        .getRequest(request, response);

                if (email != null) {
                    user = userMapper.toUser(userService.getUserByEmail(email));
                } else {
                    user = userMapper.toUser(userService.getUserByUsername(username));
                }

                String accessToken = jwtUtil.generateAccessToken(user);
                String refreshToken = jwtUtil.generateRefreshToken(user);

                refreshTokenStorageService.revoke(user.getId().toString());
                refreshTokenStorageService.store(user.getId().toString(), refreshToken);

                String targetUrl = "/";

                if (clientType != null) {
                    String originalUrl = savedRequest.getRedirectUrl();

                    if (originalUrl.contains("localhost:8080")) {
                        targetUrl = UriComponentsBuilder.fromUriString("/api/auth/oauth2/success")
                                .queryParam("accessToken", accessToken)
                                .queryParam("refreshToken", refreshToken)
                                .build().toUriString();
                    }

                    response.sendRedirect(targetUrl);
                } else {
                    saveTokensToCookies(response, accessToken, refreshToken);

                    response.sendRedirect("/");
                }
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    private void saveTokensToCookies(HttpServletResponse response, String access, String refresh) {
        Cookie aCookie = new Cookie("jwt_access", access);
        aCookie.setHttpOnly(true);
        aCookie.setPath("/");
        aCookie.setMaxAge((int) jwtProperties.getExpiration());

        Cookie rCookie = new Cookie("jwt_refresh", refresh);
        rCookie.setHttpOnly(true);
        rCookie.setPath("/auth/refresh");
        rCookie.setMaxAge((int) jwtProperties.getRefreshToken().getExpiration());

        response.addCookie(aCookie);
        response.addCookie(rCookie);
    }
}