package com.example.kakao_login.service;

import com.example.kakao_login.dto.store.StoreDetailResponse;
import com.example.kakao_login.dto.store.TodaysPopularStoreResponse;
import com.example.kakao_login.dto.store.HotMorningSaleResponse;
import com.example.kakao_login.entity.BusinessStatus;
import com.example.kakao_login.entity.EarlybirdDeal;
import com.example.kakao_login.entity.MenuItem;
import com.example.kakao_login.entity.Store;
import com.example.kakao_login.entity.StoreView;
import com.example.kakao_login.exception.StoreDetailServiceException;
import com.example.kakao_login.exception.StoreNotFoundException;
import com.example.kakao_login.mapper.StoreDetailMapper;
import com.example.kakao_login.repository.EarlybirdDealRepository;
import com.example.kakao_login.repository.MenuItemRepository;
import com.example.kakao_login.repository.StoreRepository;
import com.example.kakao_login.repository.StoreReviewRepository;
import com.example.kakao_login.repository.StoreViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 매장 상세 조회 Service
 * - 카카오 15년차 기준 실무 구현
 * - 단일 책임: 비즈니스 로직만 담당
 * - 매핑 로직은 Mapper에 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreDetailService {

    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;
    private final EarlybirdDealRepository dealRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final StoreViewRepository storeViewRepository;
    private final UserFavoriteService userFavoriteService;
    private final StoreDetailMapper mapper;

    /**
     * 매장 상세 정보 조회
     * @param storeId 매장 ID
     * @param userId 사용자 ID (옵셔널, 향후 개인화용)
     * @return 매장 상세 응답
     * @throws StoreNotFoundException 매장을 찾을 수 없는 경우
     * @throws StoreDetailServiceException 서비스 로직 오류 발생 시
     */
    public StoreDetailResponse getStoreDetail(String storeId, String userId) {
        log.debug("매장 상세 조회 시작 - storeId: {}, userId: {}", storeId, userId);

        try {
            // 1. 매장 기본 정보 조회
            Store store = findStoreById(storeId);

            // 2. 관련 데이터 조회 (병렬 처리 가능)
            List<MenuItem> menuItems = findMenuItems(storeId);
            EarlybirdDeal currentDeal = findCurrentDeal(storeId);

            // 3. 리뷰 통계 조회
            List<com.example.kakao_login.entity.StoreReview> reviews = storeReviewRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
            Integer totalReviews = reviews.size();
            Double averageRating = reviews.isEmpty() ? 0.0 : reviews.stream()
                .mapToDouble(review -> review.getRating().doubleValue())
                .average()
                .orElse(0.0);
            averageRating = Math.round(averageRating * 10.0) / 10.0; // 소수점 1자리

            // 4. 사용자별 컨텍스트 생성
            StoreDetailResponse.UserContext userContext = createUserContext(userId, storeId);

            // 5. DTO 변환 (Mapper에 위임)
            StoreDetailResponse response = mapper.toStoreDetailResponse(
                store, menuItems, currentDeal, userContext, totalReviews, averageRating
            );

            log.debug("매장 상세 조회 완료 - storeId: {}, 메뉴수: {}", storeId, menuItems.size());
            
            // 6. 조회수 증가 (비동기 처리 가능)
            incrementStoreView(storeId);
            
            return response;

        } catch (StoreNotFoundException e) {
            throw e; // 재던짐 (Controller에서 404 처리)
        } catch (Exception e) {
            log.error("매장 상세 조회 중 예상치 못한 오류 - storeId: {}", storeId, e);
            throw new StoreDetailServiceException("매장 상세 조회 실패", e);
        }
    }

    /**
     * 활성 매장 조회
     * @param storeId 매장 ID
     * @return 매장 엔티티
     * @throws StoreNotFoundException 매장을 찾을 수 없는 경우
     */
    private Store findStoreById(String storeId) {
        return storeRepository.findActiveById(storeId)
            .orElseThrow(() -> {
                log.warn("매장을 찾을 수 없음 - storeId: {}", storeId);
                return new StoreNotFoundException(storeId);
            });
    }

    /**
     * 매장의 메뉴 목록 조회
     * @param storeId 매장 ID
     * @return 정렬된 메뉴 목록
     */
    private List<MenuItem> findMenuItems(String storeId) {
        List<MenuItem> menuItems = menuItemRepository.findActiveByStoreIdOrderBySortOrder(storeId);
        log.debug("메뉴 조회 완료 - 매장: {}, 메뉴수: {}", storeId, menuItems.size());
        return menuItems;
    }

    /**
     * 현재 유효한 할인 정보 조회
     * @param storeId 매장 ID
     * @return 할인 정보 (없으면 null)
     */
    private EarlybirdDeal findCurrentDeal(String storeId) {
        LocalDateTime now = LocalDateTime.now();
        EarlybirdDeal deal = dealRepository.findTopCurrentByStoreId(storeId, now)
            .orElse(null);
        
        if (deal != null) {
            log.debug("할인 정보 발견 - 매장: {}, 할인: {}", storeId, deal.getDisplayText());
        }
        
        return deal;
    }

    /**
     * 사용자별 컨텍스트 정보 생성
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @return 사용자 컨텍스트
     */
    private StoreDetailResponse.UserContext createUserContext(String userId, String storeId) {
        // 즐겨찾기 상태 조회
        boolean isFavorite = userFavoriteService.isFavorite(userId, storeId);
        
        // 쿠폰 상태는 기본값 (향후 쿠폰 서비스 연동)
        return mapper.createUserContext(userId, isFavorite, false);
    }

    /**
     * 매장 조회수 증가
     * @param storeId 매장 ID
     */
    @Transactional
    public void incrementStoreView(String storeId) {
        try {
            LocalDate today = LocalDate.now();
            
            // 기존 조회수 레코드 조회
            StoreView existingView = storeViewRepository.findByStoreIdAndViewDateAndIsActiveTrue(storeId, today)
                .orElse(null);

            if (existingView != null) {
                // 기존 레코드가 있으면 조회수 증가
                existingView.setViewCount(existingView.getViewCount() + 1);
                storeViewRepository.save(existingView);
            } else {
                // 새 레코드 생성
                StoreView newView = StoreView.builder()
                    .storeId(storeId)
                    .viewDate(today)
                    .viewCount(1)
                    .isActive(true)
                    .build();
                storeViewRepository.save(newView);
            }
            
            log.debug("매장 조회수 증가 완료 - storeId: {}, date: {}", storeId, today);
            
        } catch (Exception e) {
            log.error("매장 조회수 증가 중 오류 - storeId: {}", storeId, e);
            // 조회수 증가 실패는 전체 요청에 영향을 주지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 오늘의 인기 매장 조회
     * @return 오늘의 인기 매장 응답
     */
    public TodaysPopularStoreResponse getTodaysPopularStore() {
        log.debug("오늘의 인기 매장 조회 시작");

        try {
            LocalDate today = LocalDate.now();
            
            // 오늘의 최고 조회수 매장 조회
            StoreView topStoreView = storeViewRepository.findTopByViewDateOrderByViewCountDesc(today)
                .orElse(null);

            if (topStoreView == null) {
                log.warn("오늘의 조회수 데이터가 없음");
                return null; // 데이터가 없으면 null 반환
            }

            // 매장 정보 조회
            Store store = findStoreById(topStoreView.getStoreId());
            
            // 현재 할인 정보 조회
            EarlybirdDeal currentDeal = findCurrentDeal(store.getId());

            // 리뷰 평점 계산
            List<com.example.kakao_login.entity.StoreReview> reviews = storeReviewRepository.findByStoreIdOrderByCreatedAtDesc(store.getId());
            Double averageRating = reviews.isEmpty() ? 0.0 : reviews.stream()
                .mapToDouble(review -> review.getRating().doubleValue())
                .average()
                .orElse(0.0);
            averageRating = Math.round(averageRating * 10.0) / 10.0; // 소수점 1자리

            // DTO 변환
            TodaysPopularStoreResponse response = TodaysPopularStoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .repImageUrl(store.getRepImageUrl())
                .businessInfo(TodaysPopularStoreResponse.BusinessInfo.builder()
                    .status(store.getBusinessStatus().toString().toLowerCase())
                    .statusMessage(getBusinessStatusMessage(store.getBusinessStatus()))
                    .build())
                .dealInfo(currentDeal != null ? TodaysPopularStoreResponse.DealInfo.builder()
                    .title(currentDeal.getTitle())
                    .description(currentDeal.getDescription())
                    .build() : null)
                .rating(averageRating)
                .build();

            log.debug("오늘의 인기 매장 조회 완료 - storeId: {}, viewCount: {}", 
                store.getId(), topStoreView.getViewCount());
            return response;

        } catch (Exception e) {
            log.error("오늘의 인기 매장 조회 중 오류", e);
            throw new StoreDetailServiceException("오늘의 인기 매장 조회 실패", e);
        }
    }

    /**
     * 영업 상태 메시지 생성
     */
    private String getBusinessStatusMessage(BusinessStatus status) {
        switch (status) {
            case OPEN_24H:
                return "24시간 영업";
            case OPEN:
                return "영업중";
            case CLOSED:
                return "영업종료";
            case BREAK_TIME:
                return "브레이크타임";
            case PREPARING:
                return "준비중";
            case HOLIDAY:
                return "휴무일";
            default:
                return "영업정보 없음";
        }
    }

    /**
     * 오늘의 HOT 모닝 세일 조회
     * 현재 유효한 얼리버드 딜이 있는 매장들의 정보를 반환
     * 
     * @return HOT 모닝 세일 응답 DTO
     */
    public HotMorningSaleResponse getHotMorningSales() {
        log.info("오늘의 HOT 모닝 세일 조회 시작");
        
        LocalDateTime currentTime = LocalDateTime.now();
        
        // 현재 유효한 얼리버드 딜 조회
        List<EarlybirdDeal> activeDeals = dealRepository.findActiveEarlybirdDeals(currentTime);
        
        if (activeDeals.isEmpty()) {
            log.info("현재 유효한 얼리버드 딜이 없습니다.");
            return HotMorningSaleResponse.builder()
                    .stores(List.of())
                    .totalCount(0)
                    .build();
        }
        
        // 매장 ID 목록 추출
        Set<String> storeIds = activeDeals.stream()
                .map(EarlybirdDeal::getStoreId)
                .collect(Collectors.toSet());
        
        // 매장 정보 조회
        List<Store> stores = storeRepository.findByIdInAndIsActiveTrue(List.copyOf(storeIds));
        
        // 매장별 딜 정보 매핑 (매장당 최신 딜 하나만)
        Map<String, EarlybirdDeal> storeDealMap = activeDeals.stream()
                .collect(Collectors.toMap(
                    EarlybirdDeal::getStoreId,
                    deal -> deal,
                    (existing, replacement) -> existing.getCreatedAt().isAfter(replacement.getCreatedAt()) 
                            ? existing : replacement
                ));
        
        // 응답 DTO 생성
        List<HotMorningSaleResponse.HotMorningSaleStoreDto> storeDtos = stores.stream()
                .filter(store -> storeDealMap.containsKey(store.getId()))
                .map(store -> {
                    EarlybirdDeal deal = storeDealMap.get(store.getId());
                    return HotMorningSaleResponse.HotMorningSaleStoreDto.builder()
                            .storeId(store.getId())
                            .storeName(store.getName())
                            .repImageUrl(store.getRepImageUrl())
                            .displayText(deal.getDisplayText())
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("오늘의 HOT 모닝 세일 조회 완료. 매장 수: {}", storeDtos.size());
        
        return HotMorningSaleResponse.builder()
                .stores(storeDtos)
                .totalCount(storeDtos.size())
                .build();
    }
}