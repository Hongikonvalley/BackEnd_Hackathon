package com.example.kakao_login.repository;

import com.example.kakao_login.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 리뷰 이미지 Repository  
 */
@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, String> {
    
    /**
     * 여러 리뷰의 대표 이미지들을 한 번에 조회 (성능 최적화)
     * - 각 리뷰의 첫 번째 이미지만 가져옴
     */
    @Query("""
        SELECT ri FROM ReviewImage ri 
        WHERE ri.reviewId IN :reviewIds 
        AND ri.isActive = true 
        AND ri.sortOrder = (
            SELECT MIN(ri2.sortOrder) FROM ReviewImage ri2 
            WHERE ri2.reviewId = ri.reviewId 
            AND ri2.isActive = true
        )
        ORDER BY ri.reviewId, ri.sortOrder
    """)
    List<ReviewImage> findRepresentativeImagesByReviewIds(@Param("reviewIds") List<String> reviewIds);
}
