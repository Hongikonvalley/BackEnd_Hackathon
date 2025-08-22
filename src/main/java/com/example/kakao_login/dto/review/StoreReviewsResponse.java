package com.example.kakao_login.dto.review;

import com.example.kakao_login.common.ApiResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * 매장 리뷰 조회 응답 DTO
 * - UI 기반: 방문자 TMI(태그), AI 리뷰 요약, 일반 리뷰, 포토 리뷰
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoreReviewsResponse(
    @JsonProperty("ai_summary")
    AiSummary aiSummary, // AI 리뷰 요약

    @JsonProperty("photos")
    List<String> photos, // 사진 URL 리스트 (ID 없이)

    @JsonProperty("reviews")
    List<Review> reviews // 리뷰 목록
) {
    /**
     * AI 리뷰 요약 DTO
     */
    @Builder
    public record AiSummary(
        String content // 요약 내용
    ) {}

    /**
     * 리뷰 DTO
     */
    @Builder
    public record Review(
        String id, // 리뷰 ID
        @JsonProperty("user_nickname")
        String userNickname, // 작성자 닉네임
        Double rating, // 평점 (1.0-5.0)
        String content // 리뷰 내용
    ) {
        public Review {
            if (rating != null && (rating < 1.0 || rating > 5.0)) {
                throw new IllegalArgumentException("Rating must be between 1.0 and 5.0: " + rating);
            }
        }
    }

    /**
     * 성공 응답 생성 헬퍼 메서드
     */
    public static ApiResponse<StoreReviewsResponse> success(StoreReviewsResponse data) {
        return ApiResponse.success(data);
    }
}
