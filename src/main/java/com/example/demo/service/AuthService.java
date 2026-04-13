package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
     // ✅ Inject base URL from properties
    @Value("${app.base-url}")
    private String baseUrl;


    // ── REGISTER ──────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered: " + request.getEmail());
        }

        // Build new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setActive(true);

        // Save to DB
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                "Registration successful!"
            );
    }

    // ── LOGIN ─────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check if account is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is disabled");
        }

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        // 4️⃣ Determine token expiration
        long expiration = request.isRememberMe() ? 30L * 24 * 60 * 60 * 1000 : 7L * 24 * 60 * 60 * 1000;
        // 30 days vs 7 days in milliseconds

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), expiration);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                "Login successful!");
    };

 public void forgotPassword(String email) {

    String cleanEmail = email.trim().toLowerCase();
    User user = userRepository.findByEmail(cleanEmail).orElse(null);

    if (user != null) {

        // token valid for 15 minutes
        long expiration = 15 * 60 * 1000;
        String token = jwtUtil.generateToken(user.getEmail(), expiration);

          // ✅ Dynamic URL — works on both localhost and Rende
    
          String resetLink = baseUrl + "/reset-password?token=" + token;

        emailService.sendEmail(
                cleanEmail,
                "Reset Your Password",
                "Click the link below to reset your password:\n\n" + resetLink
        );
    }

    // ❗ DO NOTHING if email not found (IMPORTANT)
}


    public void resetPassword(String token, String newPassword) {

        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        String email = jwtUtil.getEmailFromToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
