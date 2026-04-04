package com.example.demo.config;

import com.example.demo.security.JwtFilter;
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

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    // ── Pages ──────────────────
                    "/",
                    "/index",
                    "/all-tools",
                    "/file-tools",
                    "/login",
                    "/register",
                    "/tool-convert-pdf",
                    "/tool-compress-pdf",
                    "/tool-lock-pdf",
                    "/tool-unlock-pdf",
                    "/tool-merge-pdf",
                    "/tool-split-pdf",
                    "/tool-convert-image",
                    "/tool-crop-image",
                    "/tool-resize-image",
                    "/tool-watermark-image",
                    "/tool-merge-images",
                    "/tool-compress-image",
                    "/tool-convert-audio",
                    "/tool-trim-audio",
                    "/tool-merge-audio",
                    "/tool-extract-audio",
                    "/tool-convert-video",
                    "/tool-trim-video",
                    "/tool-merge-videos",
                    "/tool-extract-audio-video",
                    // ── API ────────────────────
                    "/api/auth/**",
                    "/api/pdf/**",
                    "/api/image/**",
                    "/api/audio/**",
                    "/api/video/**",
                    // ── Static files ───────────
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}