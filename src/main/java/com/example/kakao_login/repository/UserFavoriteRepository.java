package com.example.kakao_login.repository;

import com.example.kakao_login.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 즐겨찾기 Repository
 * - 단순한 즐겨찾기 ON/OFF 기능
 * - 하드 삭제 방식 (삭제시 DB에서 완전 제거)
 */
@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, String> {
    
    /**
     * 특정 사용자의 특정 매장 즐겨찾기 상태 확인
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @return 즐겨찾기 정보 (있으면 Optional에 포함, 없으면 empty)
     */
    Optional<UserFavorite> findByUserIdAndStoreId(String userId, String storeId);
    
    /**
     * 특정 사용자의 모든 즐겨찾기 목록 조회 (최신 등록순)
     * @param userId 사용자 ID
     * @return 즐겨찾기 목록
     */
    @Query("""
        SELECT uf FROM UserFavorite uf 
        WHERE uf.userId = :userId 
        ORDER BY uf.createdAt DESC
    """)
    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
}
