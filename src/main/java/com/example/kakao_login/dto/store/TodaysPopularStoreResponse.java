package com.example.kakao_login.dto.store;

import com.example.kakao_login.common.ApiResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 오늘의 인기 매장 응답 DTO
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TodaysPopularStoreResponse(
    @JsonProperty("store_id")
    String storeId, // 매장 ID
    
    @JsonProperty("store_name")
    String storeName, // 매장명
    
    @JsonProperty("rep_image_url")
    String repImageUrl, // 대표사진
    
    @JsonProperty("business_info")
    BusinessInfo businessInfo, // 영업 정보
    
    @JsonProperty("deal_info")
    DealInfo dealInfo, // 할인 정보
    
    @JsonProperty("rating")
    Double rating // 평점
) {
    /**
     * 영업 정보 DTO
     */
    @Builder
    public record BusinessInfo(
        @JsonProperty("status")
        String status, // 영업 상태 (open, closed, open_24h 등)
        
        @JsonProperty("status_message")
        String statusMessage // 상태 메시지 (24시간 영업, 9시 오픈 등)
    ) {}

    /**
     * 할인 정보 DTO
     */
    @Builder
    public record DealInfo(
        @JsonProperty("title")
        String title, // 할인 제목
        
        @JsonProperty("description")
        String description // 할인 설명
    ) {}

    /**
     * 성공 응답 생성 헬퍼 메서드
     */
    public static ApiResponse<TodaysPopularStoreResponse> success(TodaysPopularStoreResponse data) {
        return ApiResponse.success(data);
    }
}
