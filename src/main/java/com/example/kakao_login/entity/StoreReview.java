package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

/**
 * 매장 리뷰 엔티티
 * - 일반/포토 리뷰 통합 관리 (이미지 존재 여부로 구분)
 */
@Entity
@Table(name = "store_reviews", indexes = {
    @Index(name = "idx_review_store_created", columnList = "store_id, created_at DESC"),
    @Index(name = "idx_review_active", columnList = "is_active"),
    @Index(name = "idx_review_rating", columnList = "rating")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReview extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 리뷰ID

    @Column(name = "store_id", nullable = false, length = 36)
    private String storeId; // 매장ID

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId; // 작성자ID

    @Column(name = "user_nickname", nullable = false, length = 50)
    private String userNickname; // 작성자 닉네임

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating; // 평점 (1.0-5.0)

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    /**
     * 평점 유효성 검증
     */
    @PrePersist @PreUpdate
    public void validateRating() {
        if (rating != null && (rating.compareTo(BigDecimal.valueOf(1.0)) < 0 || rating.compareTo(BigDecimal.valueOf(5.0)) > 0)) {
            throw new IllegalArgumentException("평점은 1.0 ~ 5.0 사이여야 합니다: " + rating);
        }
    }
}
