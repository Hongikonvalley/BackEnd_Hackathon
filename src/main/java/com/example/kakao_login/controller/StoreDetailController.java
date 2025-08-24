package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.store.StoreDetailResponse;
import com.example.kakao_login.dto.store.TodaysPopularStoreResponse;
import com.example.kakao_login.dto.store.HotMorningSaleResponse;
import com.example.kakao_login.service.StoreDetailService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 매장 상세 조회 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Validated
public class StoreDetailController {

    private final StoreDetailService storeDetailService;

    /**
     * 매장 상세 정보 조회
     */
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> getStoreDetail(
        @PathVariable 
        @NotBlank(message = "매장 ID는 필수입니다") 
        String storeId,
        
        @RequestHeader(name = "X-USER-ID", required = false) 
        String userId
    ) {
        StoreDetailResponse response = storeDetailService.getStoreDetail(storeId, userId);
        return ApiResponse.success(response);
    }

    /**
     * 오늘의 인기 매장 조회
     * @return 오늘의 인기 매장 응답
     */
    @GetMapping("/popular/today")
    public ResponseEntity<ApiResponse<TodaysPopularStoreResponse>> getTodaysPopularStore() {
        log.debug("오늘의 인기 매장 조회 요청");

        try {
            TodaysPopularStoreResponse response = storeDetailService.getTodaysPopularStore();
            
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("오늘의 인기 매장 데이터가 없습니다.", 404));
            }
            
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("오늘의 인기 매장 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("오늘의 인기 매장 조회 중 오류가 발생했습니다.", 500));
        }
    }

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
            HotMorningSaleResponse response = storeDetailService.getHotMorningSales();
            
            log.info("오늘의 HOT 모닝 세일 조회 성공. 매장 수: {}", response.getTotalCount());
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("오늘의 HOT 모닝 세일 조회 중 오류 발생", e);
            throw e;
        }
    }
}
