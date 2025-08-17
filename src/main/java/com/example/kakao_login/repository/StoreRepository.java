package com.example.kakao_login.repository;

import com.example.kakao_login.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 매장 Repository
 * - 기본 CRUD + 도메인 특화 메서드
 * - 활성 상태 매장만 조회하는 비즈니스 로직 포함
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, String> {

    /**
     * 활성 상태인 매장 조회 (매장 상세 API용)
     * @param storeId 매장 ID
     * @return 매장 정보 Optional
     */
    @Query("""
        SELECT s FROM Store s 
        WHERE s.id = :storeId 
        AND s.isActive = true
        """)
    Optional<Store> findActiveById(@Param("storeId") String storeId);
}
