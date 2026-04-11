package com.example.demo.config;

import com.example.demo.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Static files — always public
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**",
                                "/fonts/**", "/webjars/**", "/favicon.ico")
                        .permitAll()

                        // Auth pages — public
                        .requestMatchers("/login", "/register").permitAll()

                        // Auth API — public
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/logout",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password")
                        .permitAll()

                        // Everything else — must be logged in
                        .anyRequest().authenticated())

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ── KEY FIX: tell Spring what to do when not authenticated ──
                // For page requests → redirect to /login
                // For API requests → return 401 JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String path = request.getServletPath();
                            if (path.startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        }))

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}