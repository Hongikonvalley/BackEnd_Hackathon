package com.example.kakao_login.dto.store;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.entity.BusinessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * 매장 상세 조회 응답 DTO
 * - 모바일 클라이언트 최적화
 * - 필수 정보만 포함하여 응답 크기 최소화
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoreDetailResponse(
    @JsonProperty("store_id")
    String storeId, // 외부노출용ID (UUID 마스킹)
    
    String name, // 매장명
    
    String address, // 주소
    
    @JsonProperty("rep_image_url")
    String repImageUrl, // 대표 이미지 URL
    
    @JsonProperty("ai_recommendation")
    String aiRecommendation, // AI 추천 메시지
    
    Location location, // 위치정보
    
    @JsonProperty("external_links")
    ExternalLinks externalLinks, // 외부연동정보
    
    @JsonProperty("rating_avg")
    Double ratingAvg, // 평균평점 (소수점2자리)
    
    @JsonProperty("total_reviews")
    Integer totalReviews, // 총 리뷰 수
    
    @JsonProperty("average_rating")
    Double averageRating, // 리뷰 평균 평점
    
    @JsonProperty("business_info")
    BusinessInfo businessInfo, // 영업정보
    
    @JsonProperty("discount_info")
    DiscountInfo discountInfo, // 할인정보
    
    @JsonProperty("user_context")
    UserContext userContext, // 사용자별정보
    
    List<MenuItemDto> menus // 메뉴목록
) {

    /**
     * 위치 정보 그룹
     */
    @Builder
    public record Location(
        Double latitude, // 위도 (Double 6자리 정밀도)
        Double longitude // 경도 (Double 6자리 정밀도)
    ) {
        public Location {
            // 좌표 유효성 검증
            if (latitude != null && (latitude < -90 || latitude > 90)) {
                throw new IllegalArgumentException("Invalid latitude: " + latitude);
            }
            if (longitude != null && (longitude < -180 || longitude > 180)) {
                throw new IllegalArgumentException("Invalid longitude: " + longitude);
            }
        }
    }

    /**
     * 외부 연동 정보
     */
    @Builder
    public record ExternalLinks(
        @JsonProperty("kakao_place_id")
        String kakaoPlaceId, // 카카오지도ID
        
        @JsonProperty("naver_place_id")
        String naverPlaceId // 네이버지도ID
    ) {}

    /**
     * 영업 정보
     */
    @Builder
    public record BusinessInfo(
        String status, // 영업상태 (문자열로 변환)
        
        @JsonProperty("status_message")
        String statusMessage // 상태메시지
    ) {
        public static BusinessInfo from(BusinessStatus businessStatus) {
            return BusinessInfo.builder()
                .status(businessStatus.name().toLowerCase())
                .statusMessage(businessStatus.getDisplayName())
                .build();
        }
    }

    /**
     * 할인 정보 - 단순한 메시지만
     */
    public record DiscountInfo(
        String message // 할인메시지 ("오전 7시 방문 얼리버드 10% 할인")
    ) {}

    /**
     * 사용자별 컨텍스트 정보
     */
    @Builder
    public record UserContext(
        @JsonProperty("is_favorite")
        boolean isFavorite, // 즐겨찾기여부
        
        @JsonProperty("has_coupon")
        boolean hasCoupon // 쿠폰보유여부
    ) {}

    /**
     * 메뉴 정보 DTO
     */
    @Builder
    public record MenuItemDto(
        String name, // 메뉴명
        Integer price // 가격 (원 단위)
    ) {
        public MenuItemDto {
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Price cannot be negative: " + price);
            }
        }
    }

    /**
     * 성공 응답 생성 헬퍼 메서드
     */
    public static ApiResponse<StoreDetailResponse> success(StoreDetailResponse data) {
        return ApiResponse.success(data);
    }

    /**
     * Entity에서 DTO로 변환하는 팩토리 메서드
     */
    public static class Factory {
        
        /**
         * Store Entity를 기반으로 StoreDetailResponse 생성
         */
        public static StoreDetailResponse from(
            com.example.kakao_login.entity.Store store,
            List<MenuItemDto> menus,
            DiscountInfo discountInfo,
            UserContext userContext,
            Integer totalReviews,
            Double averageRating
        ) {
            return StoreDetailResponse.builder()
                .storeId(maskStoreId(store.getId())) // UUID 마스킹
                .name(store.getName())
                .address(store.getAddress())
                .repImageUrl(store.getRepImageUrl())
                .aiRecommendation(store.getAiRecommendation())
                .location(Location.builder()
                    .latitude(store.getLatitude() != null ? store.getLatitude().doubleValue() : null)
                    .longitude(store.getLongitude() != null ? store.getLongitude().doubleValue() : null)
                    .build())
                .externalLinks(ExternalLinks.builder()
                    .kakaoPlaceId(store.getKakaoPlaceId())
                    .naverPlaceId(store.getNaverPlaceId())
                    .build())
                .ratingAvg(store.getRatingAvg() != null ? 
                    Math.round(store.getRatingAvg().doubleValue() * 100.0) / 100.0 : null)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .businessInfo(BusinessInfo.from(store.getBusinessStatus()))
                .discountInfo(discountInfo)
                .userContext(userContext)
                .menus(menus)
                .build();
        }

        /**
         * UUID를 외부 노출용 ID로 마스킹
         */
        private static String maskStoreId(String uuid) {
            // 실제로는 Base62 인코딩이나 해시 기반 단축 ID 사용
            return "ST" + uuid.substring(0, 8).toUpperCase();
        }
    }
}