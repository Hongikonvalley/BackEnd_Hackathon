package com.example.kakao_login.controller;

import com.example.kakao_login.dto.KakaoCodeRequest;
import com.example.kakao_login.dto.KakaoUserInfoDto;
import com.example.kakao_login.dto.TokenResponseDto;
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

    @CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000", "http://mutsa.shop", "https://mutsa.shop"})
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
    @CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000", "http://mutsa.shop", "https://mutsa.shop"})
    @PostMapping("/kakao")
    public ResponseEntity<TokenResponseDto> kakaoLogin(
            @RequestBody KakaoCodeRequest codeReq) {

        String code = codeReq.getCode();
        // 1) 인가 코드로 토큰 요청
        String tokenJson = kakaoAuthService.getAccessToken(code);
        String accessToken = kakaoAuthService.extractAccessToken(tokenJson);

        // 2) 토큰으로 사용자 정보 조회
        String userInfoJson = kakaoAuthService.getUserInfo(accessToken);
        KakaoUserInfoDto userDto = kakaoAuthService.parseUserInfo(userInfoJson);

        // 3) 회원 등록 또는 로그인 후 JWT 생성
        User user = kakaoAuthService.registerOrLogin(userDto);
        String jwt = JwtUtil.createJwt(user);

        // 4) 응답 DTO 반환
        TokenResponseDto response = new TokenResponseDto(jwt, "dummy-refresh-token");
        return ResponseEntity.ok(response);
    }

}

