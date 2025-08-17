package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * 얼리버드 할인 정보 엔티티
 * - 매장별 할인 이벤트 관리
 */
@Entity
@Table(name = "earlybird_deals", indexes = {
    @Index(name = "idx_deal_store_id", columnList = "store_id"),
    @Index(name = "idx_deal_status", columnList = "status, is_active"),
    @Index(name = "idx_deal_valid_period", columnList = "valid_from, valid_until")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarlybirdDeal extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 할인ID

    @Column(name = "store_id", nullable = false, length = 36)
    private String storeId; // 매장ID

    @Column(nullable = false, length = 100)
    private String title; // 할인제목

    @Column(columnDefinition = "TEXT")
    private String description; // 할인설명

    @Column(name = "discount_value", nullable = false, length = 10)
    private String discountValue; // 할인값

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType; // 할인타입

    @Column(name = "time_window", length = 50)
    private String timeWindow; // 적용시간대

    @Column(name = "display_text", nullable = false, length = 200)
    private String displayText; // 화면표시텍스트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DealStatus status; // 상태

    @Enumerated(EnumType.STRING)
    @Column(name = "deal_type", length = 30)
    private DealType dealType; // 딜타입

    @Column(name = "valid_from")
    private LocalDateTime validFrom; // 시작일시

    @Column(name = "valid_until")
    private LocalDateTime validUntil; // 종료일시

    /**
     * 할인 상태 열거형
     */
    public enum DealStatus {
        ACTIVE("활성"),
        INACTIVE("비활성"),
        EXPIRED("만료");

        private final String displayName;

        DealStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 할인 타입 열거형
     */
    public enum DiscountType {
        PERCENT("퍼센트"),
        AMOUNT("금액");

        private final String displayName;

        DiscountType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 딜 타입 열거형
     */
    public enum DealType {
        EARLYBIRD("얼리버드"),
        HAPPY_HOUR("해피아워"),
        SPECIAL("특가");

        private final String displayName;

        DealType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 유효기간 검증
     */
    @PrePersist
    @PreUpdate
    public void validatePeriod() {
        if (validFrom != null && validUntil != null && validFrom.isAfter(validUntil)) {
            throw new IllegalArgumentException("시작일시는 종료일시보다 이전이어야 합니다.");
        }
    }

    /**
     * 현재 유효한 할인인지 확인
     */
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == DealStatus.ACTIVE 
            && isActive()
            && (validFrom == null || !now.isBefore(validFrom))
            && (validUntil == null || !now.isAfter(validUntil));
    }
}