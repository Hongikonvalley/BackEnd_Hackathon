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

    private static final int MAX_TAGS = 5;

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
            List<String> visitorTags = extractVisitorTags(allReviews);
            StoreReviewsResponse.AiSummary aiSummary = createAiSummary(allReviews);

            // 4. 포토 리뷰와 일반 리뷰 분리
            List<String> photoReviewIds = findPhotoReviewIds(allReviews);
            List<StoreReviewsResponse.PhotoReview> photoReviews = createPhotoReviews(photoReviewIds);
            List<StoreReviewsResponse.GeneralReview> generalReviews = createGeneralReviews(allReviews);

            StoreReviewsResponse response = StoreReviewsResponse.builder()
                .visitorTags(visitorTags)
                .aiSummary(aiSummary)
                .photoReviews(photoReviews)
                .generalReviews(generalReviews)
                .build();

            log.debug("매장 리뷰 조회 완료 - storeId: {}, 총 리뷰: {}, 포토 리뷰: {}", 
                storeId, allReviews.size(), photoReviews.size());
            
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
            .visitorTags(Collections.emptyList())
            .aiSummary(StoreReviewsResponse.AiSummary.builder()
                .content("아직 리뷰가 없습니다.")
                .build())
            .photoReviews(Collections.emptyList())
            .generalReviews(Collections.emptyList())
            .build();
    }

    /**
     * 방문자 TMI 태그 추출 (최대 5개)
     * TODO: 실제로는 NLP 분석
     */
    private List<String> extractVisitorTags(List<StoreReview> reviews) {
        // 간단한 키워드 기반 태그 추출 (실제 구현시 NLP/ML 활용)
        Map<String, Integer> tagCount = new HashMap<>();
        
        String[] commonTags = {
            "맛있는", "친절한", "깨끗한", "조용한", "분위기 좋은",
            "가성비 좋은", "넓은", "아늑한", "신선한", "빠른 서비스"
        };

        for (StoreReview review : reviews) {
            if (review.getContent() != null) {
                String content = review.getContent();
                for (String tag : commonTags) {
                    if (content.contains(tag)) {
                        tagCount.merge(tag, 1, Integer::sum);
                    }
                }
            }
        }

        return tagCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(MAX_TAGS)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
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

        String ratingDescription;
        if (averageRating >= 4.5) {
            ratingDescription = "매우 높은 평점을 받고 있는";
        } else if (averageRating >= 4.0) {
            ratingDescription = "좋은 평가를 받고 있는";
        } else if (averageRating >= 3.5) {
            ratingDescription = "평균적인 평가를 받고 있는";
        } else {
            ratingDescription = "개선이 필요한";
        }

        return String.format("총 %d개의 리뷰로 %s 매장입니다. 고객들의 다양한 의견을 참고해보세요.", 
            reviews.size(), ratingDescription);
    }

    /**
     * 포토 리뷰 ID 목록 추출
     */
    private List<String> findPhotoReviewIds(List<StoreReview> allReviews) {
        List<String> allReviewIds = allReviews.stream()
            .map(StoreReview::getId)
            .collect(Collectors.toList());

        // 이미지가 있는 리뷰 ID들만 필터링
        List<ReviewImage> images = reviewImageRepository.findRepresentativeImagesByReviewIds(allReviewIds);
        
        return images.stream()
            .map(ReviewImage::getReviewId)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 포토 리뷰 목록 생성
     */
    private List<StoreReviewsResponse.PhotoReview> createPhotoReviews(List<String> photoReviewIds) {
        if (photoReviewIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReviewImage> representativeImages = reviewImageRepository
            .findRepresentativeImagesByReviewIds(photoReviewIds);

        return representativeImages.stream()
            .map(image -> StoreReviewsResponse.PhotoReview.builder()
                .id(image.getReviewId())
                .representativeImageUrl(image.getImageUrl())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 일반 리뷰 목록 생성
     */
    private List<StoreReviewsResponse.GeneralReview> createGeneralReviews(List<StoreReview> allReviews) {
        return allReviews.stream()
            .map(review -> StoreReviewsResponse.GeneralReview.builder()
                .id(review.getId())
                .userNickname(review.getUserNickname())
                .rating(review.getRating().doubleValue())
                .content(review.getContent())
                .build())
            .collect(Collectors.toList());
    }
}
