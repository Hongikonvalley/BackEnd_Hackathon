package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

/**
 * 리뷰 이미지 엔티티
 * - S3에 저장된 이미지 URL만 관리
 * - 1개 리뷰당 여러 이미지 가능
 */
@Entity
@Table(name = "review_images", indexes = {
    @Index(name = "idx_image_review_order", columnList = "review_id, sort_order"),
    @Index(name = "idx_image_active", columnList = "is_active")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 이미지ID

    @Column(name = "review_id", nullable = false, length = 36)
    private String reviewId; // 리뷰ID

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl; // S3 이미지 URL

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // 이미지 순서 (1부터 시작)

    /**
     * 정렬 순서 유효성 검증
     */
    @PrePersist @PreUpdate
    public void validateSortOrder() {
        if (sortOrder != null && sortOrder < 1) {
            throw new IllegalArgumentException("정렬 순서는 1 이상이어야 합니다: " + sortOrder);
        }
    }
}
