package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.review.StoreReviewsResponse;
import com.example.kakao_login.service.StoreReviewService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 매장 리뷰 조회 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Validated
public class StoreReviewController {

    private final StoreReviewService storeReviewService;

    /**
     * 매장 리뷰 조회
     */
    @GetMapping("/{storeId}/reviews")
    public ApiResponse<StoreReviewsResponse> getStoreReviews(
        @PathVariable 
        @NotBlank(message = "매장 ID는 필수입니다") 
        String storeId
    ) {
        StoreReviewsResponse response = storeReviewService.getStoreReviews(storeId);
        return ApiResponse.success(response);
    }
}
