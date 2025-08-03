package com.example.kakao_login.controller;

import com.example.kakao_login.dto.KakaoCodeRequest;
import com.example.kakao_login.dto.KakaoUserInfoDto;
import com.example.kakao_login.entity.User;
import com.example.kakao_login.service.KakaoAuthService;
import com.example.kakao_login.util.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) throws Exception {
        String tokenJson = kakaoAuthService.getAccessToken(code);
        String accessToken = kakaoAuthService.extractAccessToken(tokenJson);
        String userInfoJson = kakaoAuthService.getUserInfo(accessToken);
        KakaoUserInfoDto dto = kakaoAuthService.parseUserInfo(userInfoJson);
        User user = kakaoAuthService.registerOrLogin(dto);
        String jwt = JwtUtil.createJwt(user);
        return ResponseEntity.ok().body(jwt);
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(HttpServletRequest request) {
        try {
            System.out.println("🔥 POST /auth/kakao 진입함");

            // Body raw 읽기
            BufferedReader reader = request.getReader();
            String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("📦 Raw Body: " + body);

            // JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> payload = mapper.readValue(body, new TypeReference<>() {});
            System.out.println("✅ Parsed code: " + payload.get("code"));

            String tokenJson = kakaoAuthService.getAccessToken(payload.get("code"));
            String accessToken = kakaoAuthService.extractAccessToken(tokenJson);
            String userInfoJson = kakaoAuthService.getUserInfo(accessToken);
            KakaoUserInfoDto dto = kakaoAuthService.parseUserInfo(userInfoJson);
            User user = kakaoAuthService.registerOrLogin(dto);

            String jwt = JwtUtil.createJwt(user);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", jwt);
            response.put("refreshToken", "dummy-refresh-token");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

}

