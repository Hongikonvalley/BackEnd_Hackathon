package com.example.kakao_login.dto;

import lombok.Data;

@Data
public class KakaoUserInfoDto {
    private Long id;  // kakaoId

    private String email;

    private String nickname;
}
