package com.example.kakao_login.repository;

import com.example.kakao_login.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {

    /** 활성 상태인 매장 단건 조회 */
    @Query("""
        SELECT s FROM Store s 
        WHERE s.id = :storeId 
          AND s.isActive = true
        """)
    Optional<Store> findActiveById(@Param("storeId") String storeId);

    /** 여러 매장 활성 상태 조회 (기존 메서드) */
    @Query("""
        SELECT s FROM Store s 
        WHERE s.id IN :storeIds 
          AND s.isActive = true
        ORDER BY s.name
        """)
    List<Store> findByIdInAndIsActiveTrue(@Param("storeIds") List<String> storeIds);


    default List<Store> findActiveByIdInSafe(List<String> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return List.of(); // Java 9+, JDK 17 OK
        }
        return findByIdInAndIsActiveTrue(storeIds);
    }

    /**
     * 매장명으로 매장 조회
     * @param name 매장명
     * @return 매장 정보 Optional
     */
    Optional<Store> findByName(String name);
}
