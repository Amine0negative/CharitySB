package com.charity.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10) {
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                System.out.println("=== Password Matching ===");
                System.out.println("Raw password: " + rawPassword);
                System.out.println("Encoded password: " + encodedPassword);
                boolean matches = super.matches(rawPassword, encodedPassword);
                System.out.println("Password matches: " + matches);
                return matches;
            }
            
            @Override
            public String encode(CharSequence rawPassword) {
                System.out.println("=== Password Encoding ===");
                System.out.println("Raw password: " + rawPassword);
                String encoded = super.encode(rawPassword);
                System.out.println("Encoded password: " + encoded);
                return encoded;
            }
        };
    }
}
