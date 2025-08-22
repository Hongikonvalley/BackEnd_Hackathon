package com.example.kakao_login.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            // SecurityConfig에서 EntryPoint가 401 JSON 처리하므로 여기까지 안 올 수 있음
            throw new RuntimeException("unauthorized");
        }
        return Map.of("ok", true, "email", user.getUsername());
    }
}
