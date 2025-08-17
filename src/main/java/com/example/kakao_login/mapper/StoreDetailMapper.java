package com.example.kakao_login.mapper;

import com.example.kakao_login.dto.store.StoreDetailResponse;
import com.example.kakao_login.entity.EarlybirdDeal;
import com.example.kakao_login.entity.MenuItem;
import com.example.kakao_login.entity.Store;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 매장 상세 정보 매퍼
 * - Entity ↔ DTO 변환 전담
 * - 재사용 가능한 매핑 로직
 */
@Component
public class StoreDetailMapper {

    /**
     * Store Entity들을 StoreDetailResponse로 변환
     * @param store 매장 정보
     * @param menuItems 메뉴 목록
     * @param deal 할인 정보 (nullable)
     * @param userContext 사용자별 컨텍스트 정보
     * @return 매장 상세 응답 DTO
     */
    public StoreDetailResponse toStoreDetailResponse(
        Store store,
        List<MenuItem> menuItems,
        EarlybirdDeal deal,
        StoreDetailResponse.UserContext userContext
    ) {
        // 메뉴 DTO 변환
        List<StoreDetailResponse.MenuItemDto> menuDtos = mapMenuItems(menuItems);
        
        // 할인 정보 변환
        StoreDetailResponse.DiscountInfo discountInfo = mapDiscountInfo(deal);
        
        // 최종 응답 생성
        return StoreDetailResponse.Factory.from(
            store,
            menuDtos,
            discountInfo,
            userContext
        );
    }

    /**
     * 메뉴 목록을 DTO로 변환
     * @param menuItems 메뉴 엔티티 목록
     * @return 메뉴 DTO 목록
     */
    public List<StoreDetailResponse.MenuItemDto> mapMenuItems(List<MenuItem> menuItems) {
        return menuItems.stream()
            .map(this::mapMenuItem)
            .toList();
    }

    /**
     * 단일 메뉴를 DTO로 변환
     * @param menuItem 메뉴 엔티티
     * @return 메뉴 DTO
     */
    public StoreDetailResponse.MenuItemDto mapMenuItem(MenuItem menuItem) {
        return StoreDetailResponse.MenuItemDto.builder()
            .name(menuItem.getName())
            .price(menuItem.getPrice().intValue())
            .build();
    }

    /**
     * 할인 정보를 DTO로 변환
     * @param deal 할인 엔티티 (nullable)
     * @return 할인 DTO (nullable)
     */
    public StoreDetailResponse.DiscountInfo mapDiscountInfo(EarlybirdDeal deal) {
        if (deal == null) {
            return null;
        }
        return new StoreDetailResponse.DiscountInfo(deal.getDisplayText());
    }

    /**
     * 기본 사용자 컨텍스트 생성 (향후 확장용)
     * @param userId 사용자 ID
     * @return 기본 사용자 컨텍스트
     */
    public StoreDetailResponse.UserContext createDefaultUserContext(String userId) {
        return StoreDetailResponse.UserContext.builder()
            .isFavorite(false) // TODO: 즐겨찾기 서비스 연동
            .hasCoupon(false)  // TODO: 쿠폰 서비스 연동
            .build();
    }
}
