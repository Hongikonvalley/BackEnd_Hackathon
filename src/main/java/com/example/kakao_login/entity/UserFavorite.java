package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * 사용자 즐겨찾기 정보 엔티티
 * - 단순한 즐겨찾기 ON/OFF 기능
 * - 하드 삭제 방식 (isActive 없음)
 */
@Entity
@Table(name = "user_favorites", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_store", columnNames = {"user_id", "store_id"})
    },
    indexes = {
        @Index(name = "idx_favorite_user_id", columnList = "user_id"),
        @Index(name = "idx_favorite_created", columnList = "user_id, created_at desc")
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 즐겨찾기 ID

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId; // 사용자 ID

    @Column(name = "store_id", nullable = false, length = 36)
    private String storeId; // 매장 ID

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 즐겨찾기 등록일시

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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
