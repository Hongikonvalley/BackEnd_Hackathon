package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * 사용자 포인트 엔티티
 * - 사용자별 포인트 관리
 * - 리뷰 작성/삭제에 따른 포인트 변경
 */
@Entity
@Table(name = "user_points", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_point", columnNames = {"user_id"})
    },
    indexes = {
        @Index(name = "idx_user_point_user_id", columnList = "user_id")
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPoint extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 포인트 ID

    @Column(name = "user_id", nullable = false, length = 36, unique = true)
    private String userId; // 사용자 ID

    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance; // 포인트 잔액 (기본값: 0)

    @Column(name = "total_earned", nullable = false)
    private Integer totalEarned; // 총 획득 포인트

    @Column(name = "total_spent", nullable = false)
    private Integer totalSpent; // 총 사용 포인트

    @PrePersist
    protected void onCreate() {
        if (this.pointBalance == null) {
            this.pointBalance = 0;
        }
        if (this.totalEarned == null) {
            this.totalEarned = 0;
        }
        if (this.totalSpent == null) {
            this.totalSpent = 0;
        }
    }

    /**
     * 포인트 획득 (리뷰 작성 시)
     * @param points 획득할 포인트
     */
    public void earnPoints(int points) {
        this.pointBalance += points;
        this.totalEarned += points;
    }

    /**
     * 포인트 차감 (리뷰 삭제 시)
     * @param points 차감할 포인트
     */
    public void spendPoints(int points) {
        int actualSpent = Math.min(this.pointBalance, points); // 0보다 작아지지 않도록
        this.pointBalance -= actualSpent;
        this.totalSpent += actualSpent;
    }

    /**
     * 팩토리 메서드: 새로운 사용자 포인트 생성
     * @param userId 사용자 ID
     * @return 새로운 UserPoint 인스턴스
     */
    public static UserPoint create(String userId) {
        return UserPoint.builder()
            .userId(userId)
            .pointBalance(0)
            .totalEarned(0)
            .totalSpent(0)
            .build();
    }
}
