package com.example.kakao_login.repository;

import com.example.kakao_login.entity.StoreReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * 매장 리뷰 Repository
 */
@Repository
public interface StoreReviewRepository extends JpaRepository<StoreReview, String> {
    
    /**
     * 특정 매장의 모든 리뷰 조회 (최신순)
     * - 평균 평점, 총 리뷰 수는 조회된 결과에서 계산
     */
    @Query("""
        SELECT sr FROM StoreReview sr 
        WHERE sr.storeId = :storeId 
        AND sr.isActive = true 
        ORDER BY sr.createdAt DESC
    """)
    List<StoreReview> findByStoreIdOrderByCreatedAtDesc(@Param("storeId") String storeId);

    /**
     * 특정 매장의 리뷰 개수 조회
     */
    long countByStoreId(String storeId);

    /**
     * 매장의 모든 리뷰 삭제
     * @param storeId 매장 ID
     */
    void deleteByStoreId(String storeId);

    /**
     * 특정 사용자의 리뷰 목록 조회 (최신순)
     */
    @Query("""
        SELECT sr FROM StoreReview sr 
        WHERE sr.userId = :userId 
        AND sr.isActive = true 
        ORDER BY sr.createdAt DESC
    """)
    List<StoreReview> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    /**
     * 특정 사용자의 리뷰 개수 조회
     */
    long countByUserId(String userId);

}
