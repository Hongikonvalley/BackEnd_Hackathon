package com.example.kakao_login.repository;

import com.example.kakao_login.entity.MenuBoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메뉴판 이미지 Repository
 * - 매장별 메뉴판 이미지 관리
 */
@Repository
public interface MenuBoardImageRepository extends JpaRepository<MenuBoardImage, String> {

    /**
     * 매장의 활성 메뉴판 이미지 목록 조회 (정렬순서대로)
     * @param storeId 매장 ID
     * @return 정렬된 메뉴판 이미지 목록
     */
    @Query("""
        SELECT mbi FROM MenuBoardImage mbi 
        WHERE mbi.storeId = :storeId 
        AND mbi.isActive = true 
        ORDER BY mbi.sortOrder ASC, mbi.createdAt ASC
        """)
    List<MenuBoardImage> findActiveByStoreIdOrderBySortOrder(@Param("storeId") String storeId);

    /**
     * 매장의 메뉴판 이미지 개수 조회
     * @param storeId 매장 ID
     * @return 메뉴판 이미지 개수
     */
    long countByStoreId(String storeId);

    /**
     * 매장의 모든 메뉴판 이미지 삭제
     * @param storeId 매장 ID
     */
    void deleteByStoreId(String storeId);
}
