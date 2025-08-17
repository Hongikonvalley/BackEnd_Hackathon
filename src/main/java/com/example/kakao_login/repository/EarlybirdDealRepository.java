package com.example.kakao_login.repository;

import com.example.kakao_login.entity.EarlybirdDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 얼리버드 할인 Repository  
 * - 기본 CRUD + 유효한 할인 조회
 */
@Repository
public interface EarlybirdDealRepository extends JpaRepository<EarlybirdDeal, String> {

    /**
     * 매장의 우선순위 할인 정보 조회 (매장 상세 API용)
     * @param storeId 매장 ID
     * @param currentTime 현재 시간
     * @return 우선 할인 정보
     */
    @Query("""
        SELECT d FROM EarlybirdDeal d 
        WHERE d.storeId = :storeId 
        AND d.isActive = true 
        AND d.status = 'ACTIVE'
        AND (d.validFrom IS NULL OR d.validFrom <= :currentTime)
        AND (d.validUntil IS NULL OR d.validUntil >= :currentTime)
        ORDER BY d.createdAt DESC
        LIMIT 1
        """)
    Optional<EarlybirdDeal> findTopCurrentByStoreId(
        @Param("storeId") String storeId,
        @Param("currentTime") LocalDateTime currentTime
    );
}
