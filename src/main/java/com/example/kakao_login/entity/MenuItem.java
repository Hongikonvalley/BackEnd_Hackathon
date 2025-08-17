package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

/**
 * 메뉴 정보 엔티티
 * - 매장별 메뉴 관리
 */
@Entity
@Table(name = "menu_items", indexes = {
    @Index(name = "idx_menu_store_id", columnList = "store_id"),
    @Index(name = "idx_menu_sort_order", columnList = "store_id, sort_order")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 메뉴ID

    @Column(name = "store_id", nullable = false, length = 36)
    private String storeId; // 매장ID

    @Column(nullable = false, length = 100)
    private String name; // 메뉴명

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price; // 가격

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // 정렬순서

    @PrePersist
    @PreUpdate
    public void validatePrice() {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("메뉴 가격은 0원 이상이어야 합니다.");
        }
        if (sortOrder == null || sortOrder < 1) {
            throw new IllegalArgumentException("정렬순서는 1 이상이어야 합니다.");
        }
    }
}