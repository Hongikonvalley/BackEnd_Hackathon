package com.example.kakao_login.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 API 허용
                .allowedOrigins("http://localhost:3000")  // 프론트 주소
                .allowedMethods("*")                     // 모든 HTTP 메서드 허용 (GET, POST 등)
                .allowCredentials(true);                 // 쿠키/인증 정보 허용 (필요 시)
    }
}
