package com.example.kakao_login.dto.review;

import com.example.kakao_login.common.ApiResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * 사용자 리뷰 목록 조회 응답 DTO
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserReviewResponse(
    @JsonProperty("user_id")
    String userId, // 사용자 ID
    
    @JsonProperty("total_count")
    Integer totalCount, // 총 리뷰 개수
    
    @JsonProperty("reviews")
    List<UserReview> reviews // 리뷰 목록
) {
    /**
     * 사용자 리뷰 DTO
     */
    @Builder
    public record UserReview(
        @JsonProperty("review_id")
        String reviewId, // 리뷰 ID
        
        @JsonProperty("store_id")
        String storeId, // 매장 ID
        
        @JsonProperty("store_name")
        String storeName, // 매장명
        
        @JsonProperty("content")
        String content, // 리뷰 내용
        
        @JsonProperty("rating")
        Double rating, // 평점
        
        @JsonProperty("created_at")
        String createdAt // 작성일
    ) {}

    /**
     * 성공 응답 생성 헬퍼 메서드
     */
    public static ApiResponse<UserReviewResponse> success(UserReviewResponse data) {
        return ApiResponse.success(data);
    }
}
