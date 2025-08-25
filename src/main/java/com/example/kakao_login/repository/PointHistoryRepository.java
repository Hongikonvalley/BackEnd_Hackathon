package com.example.kakao_login.repository;

import com.example.kakao_login.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 포인트 적립 내역 Repository
 */
public interface PointHistoryRepository extends JpaRepository<PointHistory, String> {

    /**
     * 사용자별 포인트 적립 내역 조회 (최신순)
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 포인트 적립 내역 페이지
     */
    Page<PointHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * 사용자별 포인트 적립 내역 조회 (최신순, 페이징 없음)
     * @param userId 사용자 ID
     * @return 포인트 적립 내역 목록
     */
    List<PointHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 사용자별 리뷰 등록 횟수 조회
     * @param userId 사용자 ID
     * @return 리뷰 등록 횟수
     */
    @Query("SELECT COUNT(ph) FROM PointHistory ph WHERE ph.userId = :userId AND ph.type = 'EARN' AND ph.reason LIKE '%리뷰 등록%'")
    long countReviewRegistrations(@Param("userId") String userId);

    /**
     * 테이블 존재 여부 확인
     * @return 테이블이 존재하면 true, 없으면 false
     */
    @Query(value = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'point_history'", nativeQuery = true)
    long checkTableExists();
}
