package com.example.kakao_login.controller;

import com.example.kakao_login.dto.KakaoCodeRequest;
import com.example.kakao_login.dto.KakaoUserInfoDto;
import com.example.kakao_login.dto.TokenResponseDto;
import com.example.kakao_login.entity.User;
import com.example.kakao_login.service.KakaoAuthService;
import com.example.kakao_login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000", "http://mutsa.shop", "https://mutsa.shop"})
    @PostMapping("/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {
        try {
            String tokenJson   = kakaoAuthService.getAccessToken(code);
            String accessToken = kakaoAuthService.extractAccessToken(tokenJson); // throws
            String userInfoJson= kakaoAuthService.getUserInfo(accessToken);
            KakaoUserInfoDto dto = kakaoAuthService.parseUserInfo(userInfoJson); // throws
            User user = kakaoAuthService.registerOrLogin(dto);
            String jwt = JwtUtil.createJwt(user);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            log.error("kakaoCallback 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "카카오 로그인 처리 실패", "detail", e.getMessage()));
        }
    }

    @CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000", "http://mutsa.shop", "https://mutsa.shop"})
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody KakaoCodeRequest codeReq) {
        try {
            String code        = codeReq.getCode();
            String tokenJson   = kakaoAuthService.getAccessToken(code);
            String accessToken = kakaoAuthService.extractAccessToken(tokenJson); // throws

            String userInfoJson= kakaoAuthService.getUserInfo(accessToken);
            KakaoUserInfoDto userDto = kakaoAuthService.parseUserInfo(userInfoJson); // throws

            User user = kakaoAuthService.registerOrLogin(userDto);
            String jwt = JwtUtil.createJwt(user);

            return ResponseEntity.ok(new TokenResponseDto(jwt, "dummy-refresh-token"));
        } catch (Exception e) {
            log.error("kakaoLogin 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "카카오 로그인 처리 실패", "detail", e.getMessage()));
        }
    }
}
