package com.example.kakao_login.repository;

import com.example.kakao_login.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메뉴 Repository
 * - 기본 CRUD + 매장별 메뉴 조회
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

    /**
     * 매장의 활성 메뉴 목록 조회 (매장 상세 API용)
     * @param storeId 매장 ID
     * @return 정렬된 메뉴 목록
     */
    @Query("""
        SELECT m FROM MenuItem m 
        WHERE m.storeId = :storeId 
        AND m.isActive = true 
        ORDER BY m.sortOrder ASC, m.createdAt ASC
        """)
    List<MenuItem> findActiveByStoreIdOrderBySortOrder(@Param("storeId") String storeId);

    /**
     * 매장의 활성 메뉴 목록 조회 (삭제용)
     * @param storeId 매장 ID
     * @return 활성 메뉴 목록
     */
    List<MenuItem> findByStoreIdAndIsActiveTrue(String storeId);



    /**
     * 매장의 메뉴 개수 조회
     * @param storeId 매장 ID
     * @return 메뉴 개수
     */
    long countByStoreId(String storeId);

    /**
     * 매장의 모든 메뉴 삭제
     * @param storeId 매장 ID
     */
    void deleteByStoreId(String storeId);
}
