package com.example.kakao_login.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 API 허용
                .allowedOrigins(
                    "http://localhost:3000",      // 로컬 개발환경
                    "https://localhost:3000",     // 로컬 HTTPS
                    "http://mutsa.shop",          // 프로덕션 도메인
                    "https://mutsa.shop"          // 프로덕션 HTTPS 도메인
                )
                .allowedMethods("*")              // 모든 HTTP 메서드 허용 (GET, POST 등)
                .allowCredentials(true)           // 쿠키/인증 정보 허용 (필요 시)
                .allowedHeaders("*");             // 모든 헤더 허용
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login"); // templates/login.html
    }
}
