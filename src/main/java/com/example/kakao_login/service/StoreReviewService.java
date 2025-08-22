package com.example.kakao_login.service;

import com.example.kakao_login.dto.review.StoreReviewsResponse;
import com.example.kakao_login.entity.ReviewImage;
import com.example.kakao_login.entity.StoreReview;
import com.example.kakao_login.exception.StoreNotFoundException;
import com.example.kakao_login.exception.StoreReviewServiceException;
import com.example.kakao_login.repository.ReviewImageRepository;
import com.example.kakao_login.repository.StoreRepository;
import com.example.kakao_login.repository.StoreReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 매장 리뷰 조회 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreReviewService {

    private final StoreRepository storeRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final ReviewImageRepository reviewImageRepository;



    /**
     * 매장 리뷰 조회
     * @param storeId 매장 ID
     * @return 매장 리뷰 응답
     * @throws StoreNotFoundException 매장을 찾을 수 없는 경우
     * @throws StoreReviewServiceException 서비스 로직 오류 발생 시
     */
    public StoreReviewsResponse getStoreReviews(String storeId) {
        log.debug("매장 리뷰 조회 시작 - storeId: {}", storeId);

        try {
            // 1. 매장 존재 여부 확인
            validateStoreExists(storeId);

            // 2. 매장의 모든 리뷰 조회 (단일 쿼리)
            List<StoreReview> allReviews = storeReviewRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
            
            if (allReviews.isEmpty()) {
                return createEmptyResponse();
            }

            // 3. 리뷰 데이터 분석
            StoreReviewsResponse.AiSummary aiSummary = createAiSummary(allReviews);

            // 4. 포토 리뷰와 일반 리뷰 분리
            List<String> photoUrls = findPhotoUrls(allReviews);
            List<StoreReviewsResponse.Review> reviews = createReviews(allReviews);

            StoreReviewsResponse response = StoreReviewsResponse.builder()
                .aiSummary(aiSummary)
                .photos(photoUrls)
                .reviews(reviews)
                .build();

            log.debug("매장 리뷰 조회 완료 - storeId: {}, 총 리뷰: {}, 사진: {}", 
                storeId, allReviews.size(), photoUrls.size());
            
            return response;

        } catch (StoreNotFoundException e) {
            throw e; // 재던짐 (Controller에서 404 처리)
        } catch (Exception e) {
            log.error("매장 리뷰 조회 중 예상치 못한 오류 - storeId: {}", storeId, e);
            throw new StoreReviewServiceException("매장 리뷰 조회 실패", e);
        }
    }

    /**
     * 매장 존재 여부 확인
     */
    private void validateStoreExists(String storeId) {
        if (!storeRepository.findActiveById(storeId).isPresent()) {
            log.warn("매장을 찾을 수 없음 - storeId: {}", storeId);
            throw new StoreNotFoundException(storeId);
        }
    }

    /**
     * 빈 응답 생성 (리뷰가 없는 경우)
     */
    private StoreReviewsResponse createEmptyResponse() {
        return StoreReviewsResponse.builder()
            .aiSummary(StoreReviewsResponse.AiSummary.builder()
                .content("아직 리뷰가 없습니다.")
                .build())
            .photos(Collections.emptyList())
            .reviews(Collections.emptyList())
            .build();
    }



    /**
     * AI 리뷰 요약 생성
     * TODO: 실제로는 OpenAI API 활용
     */
    private StoreReviewsResponse.AiSummary createAiSummary(List<StoreReview> reviews) {
        double averageRating = reviews.stream()
            .mapToDouble(review -> review.getRating().doubleValue())
            .average()
            .orElse(0.0);

        // 간단한 요약 생성 (실제로는 AI 기반)
        String summary = generateSimpleSummary(reviews, averageRating);

        return StoreReviewsResponse.AiSummary.builder()
            .content(summary)
            .build();
    }

    /**
     * 간단한 요약 텍스트 생성
     */
    private String generateSimpleSummary(List<StoreReview> reviews, double averageRating) {
        if (reviews.isEmpty()) {
            return "리뷰가 없습니다.";
        }

        // 가비애 매장의 경우 특별한 요약 제공
        return "주로 오전 6시에 방문하고 아이스 아메리카노를 추천해요\n잉뉴님께서 좋아하시는 케이크와 한 잔 어떠세요?";
    }

    /**
     * 포토 리뷰 URL 목록 추출
     */
    private List<String> findPhotoUrls(List<StoreReview> allReviews) {
        List<String> allReviewIds = allReviews.stream()
            .map(StoreReview::getId)
            .collect(Collectors.toList());

        // 이미지가 있는 리뷰 ID들만 필터링
        List<ReviewImage> images = reviewImageRepository.findRepresentativeImagesByReviewIds(allReviewIds);
        
        return images.stream()
            .map(ReviewImage::getImageUrl)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 리뷰 목록 생성
     */
    private List<StoreReviewsResponse.Review> createReviews(List<StoreReview> allReviews) {
        return allReviews.stream()
            .map(review -> StoreReviewsResponse.Review.builder()
                .id(review.getId())
                .userNickname(review.getUserNickname())
                .rating(review.getRating().doubleValue())
                .content(review.getContent())
                .build())
            .collect(Collectors.toList());
    }
}
