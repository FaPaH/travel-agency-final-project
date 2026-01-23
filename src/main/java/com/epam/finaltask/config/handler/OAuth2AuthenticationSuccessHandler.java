package com.epam.finaltask.config.handler;

import com.epam.finaltask.model.User;
import com.epam.finaltask.model.UserPrincipal;
import com.epam.finaltask.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.epam.finaltask.service.TokenStorageService;
import com.epam.finaltask.util.JwtProperties;
import com.epam.finaltask.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static com.epam.finaltask.util.CookieUtils.addCookie;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final TokenStorageService<String> refreshTokenStorageService;
    private final JwtProperties jwtProperties;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Value("${app.oauth2.redirect-uri-param:client_type}")
    private String redirectUriParam;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        refreshTokenStorageService.revoke(user.getId().toString());
        refreshTokenStorageService.store(user.getId().toString(), refreshToken);

        String clientType = getClientType(request);

        if ("external".equals(clientType)) {
            handleExternalClient(request, response, accessToken, refreshToken);
        } else {
            handleBrowserClient(request, response, authentication, accessToken, refreshToken);
        }

        clearAuthenticationAttributes(request, response);
    }

    private void handleBrowserClient(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Authentication authentication,
                                     String accessToken,
                                     String refreshToken) throws IOException, ServletException {

        addCookie(response, "jwt_access", accessToken, "/", (int) jwtProperties.getExpiration());
        addCookie(response, "jwt_refresh", refreshToken, "/auth/refresh", (int) jwtProperties.getRefreshToken().getExpiration());

        super.onAuthenticationSuccess(request, response, authentication);
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private void handleExternalClient(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String accessToken,
                                      String refreshToken) throws IOException {

        String targetUrl = UriComponentsBuilder.fromUriString("/api/auth/oauth2/success")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getClientType(HttpServletRequest request) {
        String param = request.getParameter(redirectUriParam);
        if (param != null) return param;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (redirectUriParam.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
