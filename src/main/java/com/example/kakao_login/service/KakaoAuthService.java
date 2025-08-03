package com.example.kakao_login.service;

import com.example.kakao_login.config.KakaoOAuthProperties;
import com.example.kakao_login.dto.KakaoUserInfoDto;
import com.example.kakao_login.entity.User;
import com.example.kakao_login.repository.UserRepository;
import com.example.kakao_login.util.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoOAuthProperties kakaoOAuthProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1️⃣ 인가코드로 액세스 토큰 요청
    public String getAccessToken(String code) {
        try {
            System.out.println("🔎 getAccessToken() 호출됨");
            System.out.println("🔎 전달된 code: " + code);
            System.out.println("🔎 client_id: " + kakaoOAuthProperties.getClientId());
            System.out.println("🔎 redirect_uri: " + kakaoOAuthProperties.getRedirectUri());

            String tokenUri = "https://kauth.kakao.com/oauth/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoOAuthProperties.getClientId());
            params.add("redirect_uri", kakaoOAuthProperties.getRedirectUri());
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);

            System.out.println("✅ Kakao 응답 상태 코드: " + response.getStatusCode());
            System.out.println("✅ Kakao 응답 바디: " + response.getBody());

            return response.getBody();  // JSON (access_token 포함)

        } catch (Exception e) {
            System.err.println("❌ Kakao token 요청 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



    // 2️⃣ 액세스 토큰으로 사용자 정보 요청
    public String getUserInfo(String accessToken) {
        String userInfoUri = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                userInfoUri, HttpMethod.GET, request, String.class
        );

        return response.getBody();  // JSON (사용자 정보 포함)
    }

    public KakaoUserInfoDto parseUserInfo(String userInfoJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(userInfoJson);

        KakaoUserInfoDto dto = new KakaoUserInfoDto();
        dto.setId(root.path("id").asLong());
        dto.setEmail(root.path("kakao_account").path("email").asText());
        dto.setNickname(root.path("properties").path("nickname").asText());

        return dto;
    }

    @Autowired
    private UserRepository userRepository;

    public User registerOrLogin(KakaoUserInfoDto dto) {
        return userRepository.findByKakaoId(dto.getId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(dto.getId())
                            .email(dto.getEmail())
                            .nickname(dto.getNickname())
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public String extractAccessToken(String tokenJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(tokenJson);

        System.out.println("🔍 Kakao 응답 JSON: " + tokenJson);
        System.out.println("✅ 추출된 access_token: " + root.path("access_token").asText());

        return root.path("access_token").asText();
    }
}