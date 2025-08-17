package com.example.kakao_login.service;

import com.example.kakao_login.dto.store.StoreDetailResponse;
import com.example.kakao_login.entity.EarlybirdDeal;
import com.example.kakao_login.entity.MenuItem;
import com.example.kakao_login.entity.Store;
import com.example.kakao_login.exception.StoreDetailServiceException;
import com.example.kakao_login.exception.StoreNotFoundException;
import com.example.kakao_login.mapper.StoreDetailMapper;
import com.example.kakao_login.repository.EarlybirdDealRepository;
import com.example.kakao_login.repository.MenuItemRepository;
import com.example.kakao_login.repository.StoreRepository;
import com.example.kakao_login.repository.StoreReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
            StoreDetailResponse.UserContext userContext = createUserContext(userId);

            // 5. DTO 변환 (Mapper에 위임)
            StoreDetailResponse response = mapper.toStoreDetailResponse(
                store, menuItems, currentDeal, userContext, totalReviews, averageRating
            );

            log.debug("매장 상세 조회 완료 - storeId: {}, 메뉴수: {}", storeId, menuItems.size());
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
     * @return 사용자 컨텍스트
     */
    private StoreDetailResponse.UserContext createUserContext(String userId) {
        // 현재는 기본값, 향후 즐겨찾기/쿠폰 서비스 연동
        return mapper.createDefaultUserContext(userId);
    }
}