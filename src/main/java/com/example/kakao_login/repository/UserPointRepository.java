package com.example.kakao_login.repository;

import com.example.kakao_login.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 포인트 Repository
 */
@Repository
public interface UserPointRepository extends JpaRepository<UserPoint, String> {
    
    /**
     * 사용자 ID로 포인트 정보 조회
     * @param userId 사용자 ID
     * @return 포인트 정보 Optional
     */
    Optional<UserPoint> findByUserId(String userId);
    
    /**
     * 사용자 ID로 포인트 잔액만 조회
     * @param userId 사용자 ID
     * @return 포인트 잔액 (없으면 0)
     */
    @Query("SELECT COALESCE(up.pointBalance, 0) FROM UserPoint up WHERE up.userId = :userId")
    Integer findPointBalanceByUserId(@Param("userId") String userId);
}
