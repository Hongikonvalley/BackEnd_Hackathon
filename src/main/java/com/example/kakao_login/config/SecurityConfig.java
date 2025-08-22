package com.example.kakao_login.config;

import com.example.kakao_login.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /** 1) 액추에이터 전용 체인: health/info만 허용 (나머지 액추에이터는 차단) */
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        .anyRequest().denyAll()
                );
        return http.build();
    }

    /** 2) 앱 기본 체인: 세션 기반 로그인 API */
    @Bean
    @Order(1)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(h -> h.frameOptions(f -> f.disable()))
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        // 로그인/로그아웃만 공개
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()

                        // 공개 API (필요 시 조정)
                        .requestMatchers(HttpMethod.GET, "/api/v1/search/**", "/api/v1/stores/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/stores/*/favorite").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/stores/*/favorite").permitAll()

                        // 테스트 엔드포인트 허용 (GET, POST 모두)
                        .requestMatchers(HttpMethod.GET, "/api/v1/test/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/test/**").permitAll()

                        // 그 외는 인증 필요 (여기에 /api/auth/me 포함)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((req, res, auth) -> json(res, 200, "{\"ok\":true}"))
                        .failureHandler((req, res, ex)   -> json(res, 401, "{\"ok\":false,\"error\":\"bad_credentials\"}"))
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> json(res, 200, "{\"ok\":true}"))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> json(res, 401, "{\"ok\":false,\"error\":\"unauthorized\"}"))
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 운영에서는 정확한 프론트 도메인으로 제한 권장
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private static void json(HttpServletResponse res, int status, String body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(body);
    }
}
