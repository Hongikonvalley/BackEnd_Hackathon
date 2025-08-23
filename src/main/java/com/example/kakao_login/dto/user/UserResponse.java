package com.example.kakao_login.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 사용자 정보 응답 DTO
 */
public class UserResponse {

    /**
     * 현재 사용자 정보
     */
    @Builder
    public record MeResponse(
            @JsonProperty("user_id")
            String userId,

            String nickname,

            @JsonProperty("profile_image")
            String profileImage
    ) {}
}