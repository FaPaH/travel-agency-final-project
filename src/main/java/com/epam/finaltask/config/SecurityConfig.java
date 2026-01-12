package com.epam.finaltask.config;

import com.epam.finaltask.filter.JwtAuthenticationFilter;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.*;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomOAuth2UserService oAuth2UserService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final TokenStorageService<String> refreshTokenStorageService;
    private final JwtProperties jwtProperties;

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
                        .requestMatchers("/api/auth/**", "/auth/**", "/error").permitAll()
                        .requestMatchers("/favicon.ico","/","/index", "/css/**", "/js/**", "/vouchers").permitAll()
                        .requestMatchers("/api/auth/reset-password", "/auth/reset-password").authenticated()
                        .requestMatchers("/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/auth/sign-in")
                        .defaultSuccessUrl("/index", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .deleteCookies("JSESSIONID", "jwt_access", "jwt_refresh")
                        .invalidateHttpSession(true))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                );

        return http.build();
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
                boolean isRest = checkIfRest(request);

                if (email != null) {
                    user = userMapper.toUser(userService.getUserByEmail(email));
                } else {
                    user = userMapper.toUser(userService.getUserByUsername(username));
                }

                String accessToken = jwtUtil.generateToken(user);
                String refreshToken = jwtUtil.generateRefreshToken(user);

                refreshTokenStorageService.revoke(user.getId().toString());
                refreshTokenStorageService.store(user.getId().toString(), refreshToken);

                if (isRest) {
                    String targetUrl = UriComponentsBuilder.fromUriString("/api/auth/oauth2/success")
                            .queryParam("accessToken", accessToken)
                            .queryParam("refreshToken", refreshToken)
                            .build().toUriString();

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

    private boolean checkIfRest(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer != null && referer.contains("localhost:8080"); // Например, ваш React/Vue фронтенд
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