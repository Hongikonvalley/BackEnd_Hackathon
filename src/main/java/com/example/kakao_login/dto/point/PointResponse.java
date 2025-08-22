package com.example.kakao_login.dto.point;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 포인트 응답 DTO
 */
public class PointResponse {

    /**
     * 포인트 조회 응답
     */
    @Builder
    public record PointBalanceResponse(
        @JsonProperty("user_id")
        String userId, // 사용자 ID
        
        @JsonProperty("point_balance")
        Integer pointBalance // 포인트 잔액
    ) {}
}
