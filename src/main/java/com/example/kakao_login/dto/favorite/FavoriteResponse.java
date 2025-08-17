package com.example.kakao_login.dto.favorite;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * 즐겨찾기 응답 DTO
 */
public class FavoriteResponse {

    /**
     * 즐겨찾기 추가/삭제 응답
     */
    @Builder
    public record ToggleResponse(
        @JsonProperty("is_favorite")
        boolean isFavorite, // 즐겨찾기 상태
        
        String message // 처리 결과 메시지
    ) {}

    /**
     * 즐겨찾기 매장 목록 응답
     */
    @Builder
    public record StoreListResponse(
        @JsonProperty("favorite_stores")
        List<FavoriteStore> favoriteStores // 즐겨찾기한 매장 목록
    ) {}

    /**
     * 즐겨찾기 매장 정보
     */
    @Builder
    public record FavoriteStore(
        @JsonProperty("store_id")
        String storeId, // 매장 ID
        
        @JsonProperty("store_name")
        String storeName, // 매장명
        
        @JsonProperty("visit_count")
        Integer visitCount, // 방문 횟수
        
        @JsonProperty("store_image")
        String storeImage, // 매장 대표 이미지
        
        @JsonProperty("discount_text")
        String discountText, // 할인 텍스트 (예: "10% 할인 제공")
        
        @JsonProperty("sample_menu")
        String sampleMenu // 대표 메뉴 1개
    ) {}
}
