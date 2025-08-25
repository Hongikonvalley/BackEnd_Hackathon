package com.example.kakao_login.dto.point;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 적립 내역 응답 DTO
 */
public class PointHistoryResponse {

    /**
     * 포인트 적립 내역 조회 응답
     */
    @Builder
    public record PointHistoryListResponse(
        @JsonProperty("user_id")
        String userId, // 사용자 ID
        
        @JsonProperty("point_balance")
        Integer pointBalance, // 현재 포인트 잔액
        
        @JsonProperty("total_earned")
        Integer totalEarned, // 총 획득 포인트
        
        @JsonProperty("total_spent")
        Integer totalSpent, // 총 사용 포인트
        
        @JsonProperty("history")
        List<PointHistoryItem> history // 포인트 적립 내역 목록
    ) {}

    /**
     * 포인트 적립 내역 아이템
     */
    @Builder
    public record PointHistoryItem(
        @JsonProperty("id")
        String id, // 포인트 내역 ID
        
        @JsonProperty("type")
        String type, // 내역 타입 (EARN: 적립, SPEND: 사용)
        
        @JsonProperty("points")
        Integer points, // 포인트 변화량
        
        @JsonProperty("reason")
        String reason, // 적립/사용 사유
        
        @JsonProperty("created_at")
        LocalDateTime createdAt // 생성일시
    ) {}
}
