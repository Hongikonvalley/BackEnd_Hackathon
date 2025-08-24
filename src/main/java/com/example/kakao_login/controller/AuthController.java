package com.example.kakao_login.controller;

import com.example.kakao_login.dto.auth.MeResponse;
import com.example.kakao_login.entity.User;
import com.example.kakao_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal(expression = "username") String email) {
        if (email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");

        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));

        String nickname = Optional.ofNullable(u.getNickname())
                .filter(s -> !s.isBlank())
                .orElse(fallbackFromEmail(u.getEmail()));

        String avatar = Optional.ofNullable(u.getProfileImageUrl())
                .filter(s -> !s.isBlank())
                .orElse(null); // null이면 JSON에서 빠짐

        return ResponseEntity.ok(new MeResponse(true, u.getEmail(), nickname, avatar));
    }

    private String fallbackFromEmail(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
