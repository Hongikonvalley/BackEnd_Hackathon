package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.deal.HotMorningSaleResponse;
import com.example.kakao_login.service.EarlybirdDealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 얼리버드 딜 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Slf4j
public class EarlybirdDealController {

    private final EarlybirdDealService earlybirdDealService;

    /**
     * 오늘의 HOT 모닝 세일 조회
     * 현재 유효한 얼리버드 딜이 있는 매장들의 정보를 조회합니다.
     * 
     * @return HOT 모닝 세일 응답
     */
    @GetMapping("/morning-sale")
    public ApiResponse<HotMorningSaleResponse> getHotMorningSales() {
        log.info("오늘의 HOT 모닝 세일 조회 API 호출");
        
        try {
            HotMorningSaleResponse response = earlybirdDealService.getHotMorningSales();
            
            log.info("오늘의 HOT 모닝 세일 조회 성공. 매장 수: {}", response.getTotalCount());
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("오늘의 HOT 모닝 세일 조회 중 오류 발생", e);
            throw e;
        }
    }
}
