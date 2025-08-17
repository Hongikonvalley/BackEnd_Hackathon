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
    @JsonProperty("visitor_tags")
    List<String> visitorTags, // 방문자 TMI 태그 (최대 5개)

    @JsonProperty("ai_summary")
    AiSummary aiSummary, // AI 리뷰 요약

    @JsonProperty("photo_reviews")
    List<PhotoReview> photoReviews, // 포토 리뷰 목록 (이미지와 ID만)

    @JsonProperty("general_reviews")
    List<GeneralReview> generalReviews // 일반 리뷰 목록
) {
    /**
     * AI 리뷰 요약 DTO
     */
    @Builder
    public record AiSummary(
        String content, // 요약 내용
        @JsonProperty("total_reviews")
        Integer totalReviews, // 총 리뷰 수
        @JsonProperty("average_rating")
        Double averageRating // 평균 평점
    ) {}

    /**
     * 일반 리뷰 DTO
     */
    @Builder
    public record GeneralReview(
        String id, // 리뷰 ID
        @JsonProperty("user_nickname")
        String userNickname, // 작성자 닉네임
        Double rating, // 평점 (1.0-5.0)
        String content, // 리뷰 내용
        @JsonProperty("created_at")
        String createdAt // 작성일시 (ISO 8601 형식)
    ) {
        public GeneralReview {
            if (rating != null && (rating < 1.0 || rating > 5.0)) {
                throw new IllegalArgumentException("Rating must be between 1.0 and 5.0: " + rating);
            }
        }
    }

    /**
     * 포토 리뷰 DTO (대표 이미지와 ID만)
     */
    @Builder
    public record PhotoReview(
        String id, // 리뷰 ID
        @JsonProperty("representative_image_url")
        String representativeImageUrl, // 대표 이미지 URL (첫 번째 이미지)
        @JsonProperty("image_count")
        Integer imageCount // 전체 이미지 수
    ) {}

    /**
     * 성공 응답 생성 헬퍼 메서드
     */
    public static ApiResponse<StoreReviewsResponse> success(StoreReviewsResponse data) {
        return ApiResponse.success(data);
    }
}
