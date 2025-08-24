package com.example.kakao_login.service;

import com.example.kakao_login.dto.deal.HotMorningSaleResponse;
import com.example.kakao_login.entity.EarlybirdDeal;
import com.example.kakao_login.entity.Store;
import com.example.kakao_login.repository.EarlybirdDealRepository;
import com.example.kakao_login.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 얼리버드 딜 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EarlybirdDealService {

    private final EarlybirdDealRepository earlybirdDealRepository;
    private final StoreRepository storeRepository;

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
        List<EarlybirdDeal> activeDeals = earlybirdDealRepository.findActiveEarlybirdDeals(currentTime);
        
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
