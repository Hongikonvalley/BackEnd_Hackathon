package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.store.StoreDetailResponse;
import com.example.kakao_login.service.StoreDetailService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
