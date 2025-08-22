package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.review.ReviewUpdateRequest;
import com.example.kakao_login.dto.review.ReviewCreateRequest;
import com.example.kakao_login.dto.review.StoreReviewsResponse;
import com.example.kakao_login.exception.ReviewAccessDeniedException;
import com.example.kakao_login.exception.ReviewNotFoundException;
import com.example.kakao_login.exception.StoreNotFoundException;
import com.example.kakao_login.exception.StoreReviewServiceException;
import com.example.kakao_login.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 매장 리뷰 Controller
 * - 리뷰 조회, 수정, 삭제 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreReviewController {

    private final StoreReviewService storeReviewService;

    /**
     * 매장 리뷰 목록 조회
     * @param storeId 매장 ID
     * @return 매장 리뷰 응답
     */
    @GetMapping("/{storeId}/reviews")
    public ResponseEntity<ApiResponse<StoreReviewsResponse>> getStoreReviews(@PathVariable String storeId) {
        log.debug("매장 리뷰 조회 요청 - storeId: {}", storeId);

        try {
            StoreReviewsResponse response = storeReviewService.getStoreReviews(storeId);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (StoreNotFoundException e) {
            log.warn("매장을 찾을 수 없음 - storeId: {}", storeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("매장을 찾을 수 없습니다.", 404));

        } catch (StoreReviewServiceException e) {
            log.error("매장 리뷰 조회 실패 - storeId: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("매장 리뷰 조회 중 오류가 발생했습니다.", 500));
        }
    }

    /**
     * 리뷰 작성
     * @param userId 사용자 ID (실제로는 인증 토큰에서 추출)
     * @param request 리뷰 작성 요청 데이터
     * @return 작성된 리뷰 응답
     */
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<StoreReviewsResponse.Review>> createReview(
            @RequestParam String userId, // 실제로는 인증 토큰에서 추출
            @RequestBody ReviewCreateRequest request) {
        log.debug("리뷰 작성 요청 - userId: {}, storeId: {}", userId, request.storeId());

        try {
            StoreReviewsResponse.Review response = storeReviewService.createReview(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));

        } catch (StoreNotFoundException e) {
            log.warn("매장을 찾을 수 없음 - storeId: {}", request.storeId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("매장을 찾을 수 없습니다.", 404));

        } catch (StoreReviewServiceException e) {
            log.error("리뷰 작성 실패 - storeId: {}", request.storeId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("리뷰 작성 중 오류가 발생했습니다.", 500));
        }
    }

    /**
     * 리뷰 수정
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID (실제로는 인증 토큰에서 추출)
     * @param request 수정 요청 데이터
     * @return 수정된 리뷰 응답
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<StoreReviewsResponse.Review>> updateReview(
            @PathVariable String reviewId,
            @RequestParam String userId, // 실제로는 인증 토큰에서 추출
            @RequestBody ReviewUpdateRequest request) {
        log.debug("리뷰 수정 요청 - reviewId: {}, userId: {}", reviewId, userId);

        try {
            StoreReviewsResponse.Review response = storeReviewService.updateReview(reviewId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (ReviewNotFoundException e) {
            log.warn("리뷰를 찾을 수 없음 - reviewId: {}", reviewId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("리뷰를 찾을 수 없습니다.", 404));

        } catch (ReviewAccessDeniedException e) {
            log.warn("리뷰 접근 권한 없음 - reviewId: {}, userId: {}", reviewId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("리뷰를 수정할 권한이 없습니다.", 403));

        } catch (StoreReviewServiceException e) {
            log.error("리뷰 수정 실패 - reviewId: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("리뷰 수정 중 오류가 발생했습니다.", 500));
        }
    }

    /**
     * 리뷰 삭제
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID (실제로는 인증 토큰에서 추출)
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @PathVariable String reviewId,
            @RequestParam String userId) { // 실제로는 인증 토큰에서 추출
        log.debug("리뷰 삭제 요청 - reviewId: {}, userId: {}", reviewId, userId);

        try {
            storeReviewService.deleteReview(reviewId, userId);
            return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 삭제되었습니다."));

        } catch (ReviewNotFoundException e) {
            log.warn("리뷰를 찾을 수 없음 - reviewId: {}", reviewId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("리뷰를 찾을 수 없습니다.", 404));

        } catch (ReviewAccessDeniedException e) {
            log.warn("리뷰 접근 권한 없음 - reviewId: {}, userId: {}", reviewId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("리뷰를 삭제할 권한이 없습니다.", 403));

        } catch (StoreReviewServiceException e) {
            log.error("리뷰 삭제 실패 - reviewId: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("리뷰 삭제 중 오류가 발생했습니다.", 500));
        }
    }
}
