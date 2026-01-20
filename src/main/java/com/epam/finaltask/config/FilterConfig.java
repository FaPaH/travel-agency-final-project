package com.epam.finaltask.config;

import com.epam.finaltask.filter.JwtAuthenticationFilter;
import com.epam.finaltask.filter.LoginAttemptFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> registrationJwtFilter(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<LoginAttemptFilter> registrationLoginAttemptFilter(LoginAttemptFilter filter) {
        FilterRegistrationBean<LoginAttemptFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
