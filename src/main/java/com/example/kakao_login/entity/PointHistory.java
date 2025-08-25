package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * 포인트 적립 내역 엔티티
 * - 사용자별 포인트 적립/사용 내역 관리
 */
@Entity
@Table(name = "point_history",
    indexes = {
        @Index(name = "idx_point_history_user_id", columnList = "user_id"),
        @Index(name = "idx_point_history_created_at", columnList = "created_at")
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id; // 내역 ID

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId; // 사용자 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PointType type; // 내역 타입 (EARN: 적립, SPEND: 사용)

    @Column(name = "points", nullable = false)
    private Integer points; // 포인트 변화량

    @Column(name = "reason", nullable = false)
    private String reason; // 적립/사용 사유

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성일시

    /**
     * 포인트 내역 타입
     */
    public enum PointType {
        EARN,   // 적립
        SPEND   // 사용
    }

    /**
     * 팩토리 메서드: 포인트 적립 내역 생성
     */
    public static PointHistory createEarnHistory(String userId, int points, String reason) {
        return PointHistory.builder()
            .userId(userId)
            .type(PointType.EARN)
            .points(points)
            .reason(reason)
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * 팩토리 메서드: 포인트 사용 내역 생성
     */
    public static PointHistory createSpendHistory(String userId, int points, String reason) {
        return PointHistory.builder()
            .userId(userId)
            .type(PointType.SPEND)
            .points(points)
            .reason(reason)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
