package com.example.kakao_login.repository;

import com.example.kakao_login.entity.StoreView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 매장 조회수 Repository
 */
@Repository
public interface StoreViewRepository extends JpaRepository<StoreView, String> {

    /**
     * 특정 매장의 특정 날짜 조회수 조회
     */
    Optional<StoreView> findByStoreIdAndViewDateAndIsActiveTrue(String storeId, LocalDate viewDate);

    /**
     * 특정 날짜의 모든 매장 조회수 조회 (조회수 내림차순)
     */
    @Query("""
        SELECT sv FROM StoreView sv 
        WHERE sv.viewDate = :viewDate 
        AND sv.isActive = true 
        ORDER BY sv.viewCount DESC
    """)
    List<StoreView> findByViewDateOrderByViewCountDesc(@Param("viewDate") LocalDate viewDate);

    /**
     * 특정 날짜의 최고 조회수 매장 조회
     */
    @Query("""
        SELECT sv FROM StoreView sv 
        WHERE sv.viewDate = :viewDate 
        AND sv.isActive = true 
        ORDER BY sv.viewCount DESC 
        LIMIT 1
    """)
    Optional<StoreView> findTopByViewDateOrderByViewCountDesc(@Param("viewDate") LocalDate viewDate);

    /**
     * 특정 매장의 모든 조회수 기록 조회 (삭제용)
     */
    List<StoreView> findByStoreId(String storeId);
}
