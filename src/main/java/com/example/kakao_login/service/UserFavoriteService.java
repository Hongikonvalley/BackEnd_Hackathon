package com.example.kakao_login.service;

import com.example.kakao_login.dto.favorite.FavoriteResponse;
import com.example.kakao_login.entity.Store;
import com.example.kakao_login.entity.UserFavorite;
import com.example.kakao_login.entity.EarlybirdDeal;
import com.example.kakao_login.repository.UserFavoriteRepository;
import com.example.kakao_login.repository.StoreRepository;
import com.example.kakao_login.repository.EarlybirdDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 즐겨찾기 Service - 단순한 ON/OFF 토글
 */
@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final StoreRepository storeRepository;
    private final EarlybirdDealRepository dealRepository;

    /**
     * 즐겨찾기 상태 확인
     */
    public boolean isFavorite(String userId, String storeId) {
        if (userId == null || storeId == null) return false;
        return userFavoriteRepository.findByUserIdAndStoreId(userId, storeId).isPresent();
    }

    /**
     * 즐겨찾기 추가
     */
    @Transactional
    public void addFavorite(String userId, String storeId) {
        if (!userFavoriteRepository.findByUserIdAndStoreId(userId, storeId).isPresent()) {
            userFavoriteRepository.save(UserFavorite.create(userId, storeId));
        }
    }

    /**
     * 즐겨찾기 삭제
     */
    @Transactional
    public void removeFavorite(String userId, String storeId) {
        userFavoriteRepository.findByUserIdAndStoreId(userId, storeId)
            .ifPresent(userFavoriteRepository::delete);
    }

    /**
     * 즐겨찾기한 매장 목록 조회
     */
    @Transactional(readOnly = true)
    public FavoriteResponse.StoreListResponse getFavoriteStores(String userId) {
        if (userId == null) {
            return FavoriteResponse.StoreListResponse.builder()
                .favoriteStores(List.of())
                .build();
        }

        // 1. 즐겨찾기 목록 조회
        List<UserFavorite> favorites = userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        if (favorites.isEmpty()) {
            return FavoriteResponse.StoreListResponse.builder()
                .favoriteStores(List.of())
                .build();
        }

        // 2. 매장 정보 조회
        List<String> storeIds = favorites.stream()
            .map(UserFavorite::getStoreId)
            .toList();
            
        List<Store> stores = storeRepository.findByIdInAndIsActiveTrue(storeIds);
        
        // 3. 각 매장의 할인 정보 조회
        Map<String, FavoriteResponse.DealInfo> dealInfos = getDealInfos(storeIds);

        // 4. DTO 변환
        List<FavoriteResponse.FavoriteStore> favoriteStores = stores.stream()
            .map(store -> FavoriteResponse.FavoriteStore.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .storeImage(store.getRepImageUrl())
                .dealInfo(dealInfos.get(store.getId()))
                .build())
            .collect(Collectors.toList());

        return FavoriteResponse.StoreListResponse.builder()
            .favoriteStores(favoriteStores)
            .build();
    }

    /**
     * 각 매장의 할인 정보 조회
     */
    private Map<String, FavoriteResponse.DealInfo> getDealInfos(List<String> storeIds) {
        LocalDateTime now = LocalDateTime.now();
        List<EarlybirdDeal> deals = dealRepository.findActiveDealsForStores(storeIds, now);
        
        return deals.stream()
            .collect(Collectors.toMap(
                EarlybirdDeal::getStoreId,
                deal -> FavoriteResponse.DealInfo.builder()
                    .title(deal.getTitle())
                    .description(deal.getDescription())
                    .build(),
                (existing, replacement) -> existing // 중복 시 첫 번째 값 사용
            ));
    }
}
