package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 매장 조회수 엔티티
 * - 매장별 일일 조회수를 추적
 */
@Entity
@Table(name = "store_views", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"store_id", "view_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreView extends BaseEntity {

    /**
     * 조회수를 일자와 무관하게 누적하기 위한 기본 날짜
     */
    public static final LocalDate DEFAULT_VIEW_DATE = LocalDate.of(2000, 1, 1);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "store_id", nullable = false)
    private String storeId; // 매장 ID

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate; // 조회 날짜

    @Column(name = "view_count", nullable = false)
    private Integer viewCount; // 조회수

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 활성화 여부
}
