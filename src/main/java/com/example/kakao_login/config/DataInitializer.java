package com.example.kakao_login.config;

import com.example.kakao_login.entity.User;
import com.example.kakao_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner initUser(UserRepository repo) {
        return args -> {
            String email = "mutsa@mutsa.shop";
            String rawPw = "mutsa1234!";
            repo.findByEmail(email).orElseGet(() -> repo.save(
                    User.builder()
                            .email(email)
                            .password(encoder.encode(rawPw))
                            .nickname("잉뉴")
                            .build()
            ));
        };
    }
}
