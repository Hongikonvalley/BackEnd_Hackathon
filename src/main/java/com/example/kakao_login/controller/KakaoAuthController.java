package com.example.kakao_login.controller;

import com.example.kakao_login.dto.KakaoCodeRequest;
import com.example.kakao_login.dto.KakaoUserInfoDto;
import com.example.kakao_login.entity.User;
import com.example.kakao_login.service.KakaoAuthService;
import com.example.kakao_login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<?> kakaoLogin(@RequestBody KakaoCodeRequest codeRequest) {
        try {
            String code = codeRequest.getCode();
            log.info("📥 받은 code: {}", code); // 디버깅용 로그

            String tokenJson = kakaoAuthService.getAccessToken(code);
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
            log.error("🔥 카카오 로그인 처리 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kakao 인증 중 오류 발생: " + e.getMessage());
        }
    }

}

