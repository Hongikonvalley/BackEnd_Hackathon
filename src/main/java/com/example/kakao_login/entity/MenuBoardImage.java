package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

/**
 * 메뉴판 이미지 정보 엔티티
 * - 매장별 메뉴판 이미지 관리
 */
@Entity
@Table(name = "menu_board_images", indexes = {
    @Index(name = "idx_menu_board_store_id", columnList = "store_id"),
    @Index(name = "idx_menu_board_sort_order", columnList = "store_id, sort_order")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuBoardImage extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 메뉴판이미지ID

    @Column(name = "store_id", nullable = false, length = 36)
    private String storeId; // 매장ID

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl; // 이미지URL

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // 정렬순서

    @PrePersist
    @PreUpdate
    public void validateSortOrder() {
        if (sortOrder == null || sortOrder < 1) {
            throw new IllegalArgumentException("정렬순서는 1 이상이어야 합니다.");
        }
    }
}