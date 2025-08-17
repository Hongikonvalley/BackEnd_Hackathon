package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

/**
 * 매장 정보 엔티티
 * - 매장 기본정보, 위치정보, 지도연동, 평점 관리
 */
@Entity
@Table(name = "stores", indexes = {
    @Index(name = "idx_store_user_id", columnList = "user_id"),
    @Index(name = "idx_store_category_id", columnList = "category_id"),
    @Index(name = "idx_store_location", columnList = "latitude, longitude"),
    @Index(name = "idx_store_status", columnList = "business_status, is_active")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 매장ID

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId; // 사장ID

    @Column(name = "category_id", length = 36)
    private String categoryId; // 카테고리ID

    @Column(nullable = false, length = 100)
    private String name; // 매장명

    @Column(name = "ai_recommendation", columnDefinition = "TEXT")
    private String aiRecommendation; // AI추천메시지

    @Column(length = 20)
    private String phone; // 전화번호

    @Column(nullable = false, length = 200)
    private String address; // 주소

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude; // 위도

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude; // 경도

    @Column(name = "kakao_place_id", length = 50)
    private String kakaoPlaceId; // 카카오지도ID

    @Column(name = "naver_place_id", length = 50)
    private String naverPlaceId; // 네이버지도ID

    @Column(name = "rep_image_url", length = 500)
    private String repImageUrl; // 대표이미지URL

    @Column(name = "rating_avg", precision = 3, scale = 2)
    private BigDecimal ratingAvg; // 평균평점 (비정규화)

    @Column(name = "rating_count", columnDefinition = "INT DEFAULT 0")
    private Integer ratingCount; // 리뷰수 (비정규화)

    @Enumerated(EnumType.STRING)
    @Column(name = "business_status", nullable = false, length = 20)
    private BusinessStatus businessStatus; // 영업상태



    /**
     * 지도 플레이스 ID 검증
     */
    public void validatePlaceIds() {
        if (kakaoPlaceId != null && !kakaoPlaceId.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("카카오 PlaceID 형식이 올바르지 않습니다.");
        }
        if (naverPlaceId != null && !naverPlaceId.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("네이버 PlaceID 형식이 올바르지 않습니다.");
        }
    }

    /**
     * 평점 업데이트 (비정규화 필드 관리)
     */
    public void updateRating(BigDecimal newAvg, Integer newCount) {
        this.ratingAvg = newAvg;
        this.ratingCount = newCount;
    }
}