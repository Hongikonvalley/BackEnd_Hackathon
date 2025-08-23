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
     * 할인 정보
     */
    @Builder
    public record DealInfo(
        String title, // 할인 제목
        String description // 할인 설명
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
        
        @JsonProperty("store_image")
        String storeImage, // 매장 대표 이미지
        
        @JsonProperty("business_status")
        String businessStatus, // 영업 상태 (24시간 영업, 지금 영업중, n시 오픈 등)
        
        @JsonProperty("deal_info")
        DealInfo dealInfo // 할인 정보
    ) {}
}
