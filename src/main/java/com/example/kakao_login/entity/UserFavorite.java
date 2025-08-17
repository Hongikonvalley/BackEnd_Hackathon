package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 즐겨찾기 정보 엔티티
 * - 사용자가 즐겨찾기한 매장 정보를 관리
 * - 사용자와 매장 간의 N:M 관계를 1:N으로 정규화
 */
@Entity
@Table(name = "user_favorites", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_store", columnNames = {"user_id", "store_id"})
    },
    indexes = {
        @Index(name = "idx_favorite_user_id", columnList = "user_id"),
        @Index(name = "idx_favorite_store_id", columnList = "store_id"),
        @Index(name = "idx_favorite_created", columnList = "user_id, created_at desc")
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id; // 즐겨찾기 ID

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId; // 사용자 ID

    @Column(name = "store_id", nullable = false, length = 36)
    private String storeId; // 매장 ID

    /**
     * 비즈니스 로직: 즐겨찾기 상태 확인
     * @return 활성 상태 여부
     */
    public boolean isActive() {
        return super.getIsActive();
    }

    /**
     * 팩토리 메서드: 새로운 즐겨찾기 생성
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @return 새로운 UserFavorite 인스턴스
     */
    public static UserFavorite create(String userId, String storeId) {
        return UserFavorite.builder()
            .userId(userId)
            .storeId(storeId)
            .build();
    }
}
