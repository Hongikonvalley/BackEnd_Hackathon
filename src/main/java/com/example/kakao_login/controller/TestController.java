package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.entity.*;
import com.example.kakao_login.repository.*;
import com.example.kakao_login.service.UserPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    private final StoreReviewRepository storeReviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserPointService userPointService;

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
                    storeMap.put("open_time", store.getOpenTime());
                    storeMap.put("close_time", store.getCloseTime());
                    storeMap.put("business_status", store.getBusinessStatus());
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

    @PostMapping("/stores/gabiae/update-rep-image")
    public ApiResponse<String> updateGabiaeRepImage() {
        storeRepository.findByName("가비애").ifPresent(store -> {
            store.setRepImageUrl("https://github.com/user-attachments/assets/84d6b4aa-12e9-40de-92e4-f85a512275d6");
            storeRepository.save(store);
        });
        return ApiResponse.success("가비애 매장 대표 이미지가 업데이트되었습니다.");
    }

    /**
     * 오늘의 조회수 데이터 확인 (테스트용)
     */
    @GetMapping("/store-views/today")
    public ApiResponse<Object> getTodayStoreViews() {
        java.time.LocalDate defaultDate = com.example.kakao_login.entity.StoreView.DEFAULT_VIEW_DATE;
        var views = storeViewRepository.findByViewDateOrderByViewCountDesc(defaultDate);
        
        var result = new HashMap<String, Object>();
        result.put("date", defaultDate.toString());
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
            java.time.LocalDate defaultDate = com.example.kakao_login.entity.StoreView.DEFAULT_VIEW_DATE;
            
            // 기존 조회수 레코드 조회
            var existingView = storeViewRepository.findByStoreIdAndViewDateAndIsActiveTrue(storeId, defaultDate)
                .orElse(null);

            if (existingView != null) {
                // 기존 레코드가 있으면 조회수 증가
                existingView.setViewCount(existingView.getViewCount() + 1);
                storeViewRepository.save(existingView);
            } else {
                // 새 레코드 생성
                var newView = com.example.kakao_login.entity.StoreView.builder()
                    .storeId(storeId)
                        .viewDate(defaultDate)
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

    /**
     * 포인트 테스트 API
     */
    @PostMapping("/points/earn")
    public ApiResponse<String> earnPoints(@RequestParam String userId, @RequestParam int points) {
        try {
            userPointService.earnPoints(userId, points);
            return ApiResponse.success("포인트 획득 완료: " + points + "포인트");
        } catch (Exception e) {
            return ApiResponse.fail("포인트 획득 실패: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/points/spend")
    public ApiResponse<String> spendPoints(@RequestParam String userId, @RequestParam int points) {
        try {
            userPointService.spendPoints(userId, points);
            return ApiResponse.success("포인트 차감 완료: " + points + "포인트");
        } catch (Exception e) {
            return ApiResponse.fail("포인트 차감 실패: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/stores/gabiae/update-info")
    public ApiResponse<String> updateGabiaeInfo() {
        Store store = storeRepository.findByName("가비애").orElse(null);
        if (store == null) {
            return ApiResponse.fail("가비애 매장을 찾을 수 없습니다.", 404);
        }
        
        // 대표 이미지와 AI 추천 업데이트
        store.setRepImageUrl("https://github.com/user-attachments/assets/84d6b4aa-12e9-40de-92e4-f85a512275d6");
        store.setAiRecommendation("주로 오전 6시에 방문하고 아이스 아메리카노를 추천해요\n잉뉴님께서 좋아하시는 케이크와 한 잔 어떠세요?");
        storeRepository.save(store);
        
        return ApiResponse.success("가비애 매장 정보 업데이트 완료");
    }

    @DeleteMapping("/stores/{storeName}/complete")
    public ApiResponse<String> completeDeleteStore(@PathVariable String storeName) {
        try {
            Store store = storeRepository.findByName(storeName).orElse(null);
            if (store == null) {
                return ApiResponse.fail(storeName + " 매장을 찾을 수 없습니다.", 404);
            }
            
            String storeId = store.getId();
            int deletedCount = 0;
            
            // 1. 리뷰 이미지 삭제
            List<StoreReview> reviews = storeReviewRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
            if (!reviews.isEmpty()) {
                List<String> reviewIds = reviews.stream().map(StoreReview::getId).toList();
                List<ReviewImage> reviewImages = reviewImageRepository.findByReviewIdIn(reviewIds);
                reviewImageRepository.deleteAll(reviewImages);
                deletedCount += reviewImages.size();
            }
            
            // 2. 리뷰 삭제
            storeReviewRepository.deleteAll(reviews);
            deletedCount += reviews.size();
            
            // 3. 메뉴 삭제
            List<MenuItem> menus = menuItemRepository.findByStoreIdAndIsActiveTrue(storeId);
            menuItemRepository.deleteAll(menus);
            deletedCount += menus.size();
            
            // 4. 할인 정보 삭제
            List<EarlybirdDeal> deals = earlybirdDealRepository.findByStoreIdAndIsActiveTrue(storeId);
            earlybirdDealRepository.deleteAll(deals);
            deletedCount += deals.size();
            
            // 5. 매장 조회수 삭제
            List<StoreView> storeViews = storeViewRepository.findByStoreId(storeId);
            storeViewRepository.deleteAll(storeViews);
            deletedCount += storeViews.size();
            
            // 6. 매장 삭제
            storeRepository.delete(store);
            deletedCount += 1;
            
            return ApiResponse.success(storeName + " 매장 및 관련 데이터 완전 삭제 완료 (총 " + deletedCount + "개 항목 삭제)");
            
        } catch (Exception e) {
            return ApiResponse.fail("매장 삭제 중 오류 발생: " + e.getMessage(), 500);
        }
    }
}
