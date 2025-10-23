package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // API endpoints CORS
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*") // Allow all origins including ngrok
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // Re-enable for session support
                
        // VNPay endpoints CORS - FIX: Thêm mapping riêng cho VNPay
        registry.addMapping("/vnpay/**")
                .allowedOriginPatterns("*") // Allow all origins including ngrok
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // Consistent với API mapping
    }
}