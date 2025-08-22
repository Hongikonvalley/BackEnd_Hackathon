package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.repository.StoreRepository;
import com.example.kakao_login.repository.StoreViewRepository;
import com.example.kakao_login.repository.EarlybirdDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final StoreRepository storeRepository;
    private final StoreViewRepository storeViewRepository;
    private final EarlybirdDealRepository earlybirdDealRepository;

    @GetMapping("/stores")
    public ApiResponse<List<Map<String, Object>>> getStores() {
        List<Map<String, Object>> stores = storeRepository.findAll().stream()
                .map(store -> {
                    Map<String, Object> storeMap = new HashMap<>();
                    storeMap.put("id", store.getId());
                    storeMap.put("name", store.getName());
                    storeMap.put("address", store.getAddress());
                    storeMap.put("kakao_place_id", store.getKakaoPlaceId());
                    storeMap.put("naver_place_id", store.getNaverPlaceId());
                    return storeMap;
                })
                .toList();
        return ApiResponse.success(stores);
    }

    @PostMapping("/stores/gabiae/update-links")
    public ApiResponse<String> updateGabiaeLinks() {
        storeRepository.findByName("가비애").ifPresent(store -> {
            store.setKakaoPlaceId("20809319");
            store.setNaverPlaceId("1071920016");
            storeRepository.save(store);
        });
        return ApiResponse.success("가비애 매장 외부 링크 정보가 업데이트되었습니다.");
    }

    /**
     * 오늘의 조회수 데이터 확인 (테스트용)
     */
    @GetMapping("/store-views/today")
    public ApiResponse<Object> getTodayStoreViews() {
        java.time.LocalDate today = java.time.LocalDate.now();
        var views = storeViewRepository.findByViewDateOrderByViewCountDesc(today);
        
        var result = new HashMap<String, Object>();
        result.put("date", today.toString());
        result.put("total_views", views.size());
        result.put("views", views.stream().map(view -> {
            var viewData = new HashMap<String, Object>();
            viewData.put("store_id", view.getStoreId());
            viewData.put("view_count", view.getViewCount());
            return viewData;
        }).collect(java.util.stream.Collectors.toList()));
        
        return ApiResponse.success(result);
    }

    /**
     * 조회수 증가 테스트 (테스트용)
     */
    @PostMapping("/store-views/increment")
    public ApiResponse<String> incrementStoreView(@RequestParam String storeId) {
        // StoreDetailService를 직접 주입받지 않고 여기서 로직 구현
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            
            // 기존 조회수 레코드 조회
            var existingView = storeViewRepository.findByStoreIdAndViewDateAndIsActiveTrue(storeId, today)
                .orElse(null);

            if (existingView != null) {
                // 기존 레코드가 있으면 조회수 증가
                existingView.setViewCount(existingView.getViewCount() + 1);
                storeViewRepository.save(existingView);
            } else {
                // 새 레코드 생성
                var newView = com.example.kakao_login.entity.StoreView.builder()
                    .storeId(storeId)
                    .viewDate(today)
                    .viewCount(1)
                    .isActive(true)
                    .build();
                storeViewRepository.save(newView);
            }
            
            return ApiResponse.success("조회수가 증가되었습니다. storeId: " + storeId);
        } catch (Exception e) {
            return ApiResponse.fail("조회수 증가 실패: " + e.getMessage(), 500);
        }
    }

    /**
     * 매장 할인 정보 확인 (테스트용)
     */
    @GetMapping("/store-deals")
    public ApiResponse<Object> getStoreDeals(@RequestParam String storeId) {
        // 현재 시간으로 유효한 할인 정보 조회
        var currentTime = java.time.LocalDateTime.now();
        var currentDeal = earlybirdDealRepository.findTopCurrentByStoreId(storeId, currentTime);
        
        var result = new HashMap<String, Object>();
        result.put("store_id", storeId);
        result.put("current_time", currentTime.toString());
        result.put("has_active_deal", currentDeal.isPresent());
        
        if (currentDeal.isPresent()) {
            var deal = currentDeal.get();
            var dealData = new HashMap<String, Object>();
            dealData.put("id", deal.getId());
            dealData.put("title", deal.getTitle());
            dealData.put("description", deal.getDescription());
            dealData.put("status", deal.getStatus());
            dealData.put("valid_from", deal.getValidFrom());
            dealData.put("valid_until", deal.getValidUntil());
            result.put("deal", dealData);
        }
        
        return ApiResponse.success(result);
    }
}
