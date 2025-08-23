package com.example.kakao_login.service;

import com.example.kakao_login.dto.review.StoreReviewsResponse;
import com.example.kakao_login.dto.review.ReviewUpdateRequest;
import com.example.kakao_login.dto.review.ReviewCreateRequest;
import com.example.kakao_login.dto.review.UserReviewResponse;
import com.example.kakao_login.entity.ReviewImage;
import com.example.kakao_login.entity.StoreReview;
import com.example.kakao_login.exception.StoreNotFoundException;
import com.example.kakao_login.exception.StoreReviewServiceException;
import com.example.kakao_login.exception.ReviewNotFoundException;
import com.example.kakao_login.exception.ReviewAccessDeniedException;
import com.example.kakao_login.repository.ReviewImageRepository;
import com.example.kakao_login.repository.StoreRepository;
import com.example.kakao_login.repository.StoreReviewRepository;
import com.example.kakao_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final UserRepository userRepository;
    private final UserPointService userPointService;



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
     * 사용자 리뷰 목록 조회
     * @param userId 사용자 ID
     * @return 사용자 리뷰 목록 응답
     * @throws StoreReviewServiceException 서비스 로직 오류 발생 시
     */
    public UserReviewResponse getUserReviews(String userId) {
        log.debug("사용자 리뷰 목록 조회 시작 - userId: {}", userId);

        try {
            // 1. 사용자 리뷰 목록 조회
            List<StoreReview> userReviews = storeReviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            if (userReviews.isEmpty()) {
                return UserReviewResponse.builder()
                    .userId(userId)
                    .totalCount(0)
                    .reviews(Collections.emptyList())
                    .build();
            }

            // 2. 매장 정보 조회 (매장명을 위해)
            List<String> storeIds = userReviews.stream()
                .map(StoreReview::getStoreId)
                .distinct()
                .collect(Collectors.toList());

            Map<String, String> storeNameMap = storeRepository.findAllById(storeIds)
                .stream()
                .collect(Collectors.toMap(
                    store -> store.getId(),
                    store -> store.getName()
                ));

            // 3. DTO 변환
            List<UserReviewResponse.UserReview> reviews = userReviews.stream()
                .map(review -> UserReviewResponse.UserReview.builder()
                    .reviewId(review.getId())
                    .storeId(review.getStoreId())
                    .storeName(storeNameMap.getOrDefault(review.getStoreId(), "알 수 없는 매장"))
                    .content(review.getContent())
                    .rating(review.getRating().doubleValue())
                    .createdAt(review.getCreatedAt().toString())
                    .build())
                .collect(Collectors.toList());

            UserReviewResponse response = UserReviewResponse.builder()
                .userId(userId)
                .totalCount(userReviews.size())
                .reviews(reviews)
                .build();

            log.debug("사용자 리뷰 목록 조회 완료 - userId: {}, 총 리뷰: {}", userId, userReviews.size());
            return response;

        } catch (Exception e) {
            log.error("사용자 리뷰 목록 조회 중 예상치 못한 오류 - userId: {}", userId, e);
            throw new StoreReviewServiceException("사용자 리뷰 목록 조회 실패", e);
        }
    }

    /**
     * 리뷰 수정
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID (권한 확인용)
     * @param request 수정 요청 데이터
     * @return 수정된 리뷰 응답
     * @throws ReviewNotFoundException 리뷰를 찾을 수 없는 경우
     * @throws ReviewAccessDeniedException 리뷰 작성자가 아닌 경우
     * @throws StoreReviewServiceException 서비스 로직 오류 발생 시
     */
    @Transactional
    public StoreReviewsResponse.Review updateReview(String reviewId, String userId, ReviewUpdateRequest request) {
        log.debug("리뷰 수정 시작 - reviewId: {}, userId: {}", reviewId, userId);

        try {
            // 1. 리뷰 존재 여부 및 권한 확인
            StoreReview review = findReviewByIdAndValidateAccess(reviewId, userId);

            // 2. 리뷰 수정
            if (request.rating() != null) {
                review.setRating(BigDecimal.valueOf(request.rating()));
            }
            if (request.content() != null) {
                review.setContent(request.content());
            }

            StoreReview updatedReview = storeReviewRepository.save(review);

            // 3. DTO 변환
            StoreReviewsResponse.Review response = StoreReviewsResponse.Review.builder()
                .id(updatedReview.getId())
                .userNickname(updatedReview.getUserNickname())
                .rating(updatedReview.getRating().doubleValue())
                .content(updatedReview.getContent())
                .build();

            log.debug("리뷰 수정 완료 - reviewId: {}", reviewId);
            return response;

        } catch (ReviewNotFoundException | ReviewAccessDeniedException e) {
            throw e; // 재던짐 (Controller에서 적절한 HTTP 상태 코드 처리)
        } catch (Exception e) {
            log.error("리뷰 수정 중 예상치 못한 오류 - reviewId: {}", reviewId, e);
            throw new StoreReviewServiceException("리뷰 수정 실패", e);
        }
    }

    /**
     * 리뷰 삭제
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID (권한 확인용)
     * @throws ReviewNotFoundException 리뷰를 찾을 수 없는 경우
     * @throws ReviewAccessDeniedException 리뷰 작성자가 아닌 경우
     * @throws StoreReviewServiceException 서비스 로직 오류 발생 시
     */
    @Transactional
    public void deleteReview(String reviewId, String userId) {
        log.debug("리뷰 삭제 시작 - reviewId: {}, userId: {}", reviewId, userId);

        try {
            // 1. 리뷰 존재 여부 및 권한 확인
            StoreReview review = findReviewByIdAndValidateAccess(reviewId, userId);

            // 2. 포인트 차감 (리뷰 삭제 시 10포인트)
            userPointService.spendPoints(userId, 10);

            // 3. 리뷰 이미지 삭제 (CASCADE 대신 명시적 삭제)
            reviewImageRepository.deleteByReviewId(reviewId);

            // 4. 리뷰 삭제
            storeReviewRepository.delete(review);

            log.debug("리뷰 삭제 완료 - reviewId: {}, 포인트 차감: 10", reviewId);

        } catch (ReviewNotFoundException | ReviewAccessDeniedException e) {
            throw e; // 재던짐 (Controller에서 적절한 HTTP 상태 코드 처리)
        } catch (Exception e) {
            log.error("리뷰 삭제 중 예상치 못한 오류 - reviewId: {}", reviewId, e);
            throw new StoreReviewServiceException("리뷰 삭제 실패", e);
        }
    }

    /**
     * 리뷰 존재 여부 및 접근 권한 확인
     * @param reviewId 리뷰 ID
     * @param userId 사용자 ID
     * @return 리뷰 엔티티
     * @throws ReviewNotFoundException 리뷰를 찾을 수 없는 경우
     * @throws ReviewAccessDeniedException 리뷰 작성자가 아닌 경우
     */
    private StoreReview findReviewByIdAndValidateAccess(String reviewId, String userId) {
        StoreReview review = storeReviewRepository.findById(reviewId)
            .orElseThrow(() -> {
                log.warn("리뷰를 찾을 수 없음 - reviewId: {}", reviewId);
                return new ReviewNotFoundException(reviewId);
            });

        // 리뷰 작성자 확인
        if (!review.getUserId().equals(userId)) {
            log.warn("리뷰 접근 권한 없음 - reviewId: {}, userId: {}, reviewUserId: {}", 
                reviewId, userId, review.getUserId());
            throw new ReviewAccessDeniedException(reviewId, userId);
        }

        return review;
    }

    /**
     * 리뷰 작성
     * @param userId 사용자 ID
     * @param request 리뷰 작성 요청 데이터
     * @return 작성된 리뷰 응답
     * @throws StoreNotFoundException 매장을 찾을 수 없는 경우
     * @throws StoreReviewServiceException 서비스 로직 오류 발생 시
     */
    @Transactional
    public StoreReviewsResponse.Review createReview(String userId, ReviewCreateRequest request) {
        log.debug("리뷰 작성 시작 - storeId: {}, userId: {}", request.storeId(), userId);

        try {
            // 1. 입력값 유효성 검사
            request.validate();
            
            // 2. 매장 존재 여부 확인
            validateStoreExists(request.storeId());

            // 3. 사용자 정보 조회 (userId가 String이므로 email로 조회)
            String userNickname = userRepository.findByEmail(userId)
                .map(user -> user.getNickname())
                .orElse("익명사용자"); // 사용자를 찾을 수 없는 경우 기본값

            // 4. 리뷰 엔티티 생성
            StoreReview review = StoreReview.builder()
                .storeId(request.storeId())
                .userId(userId)
                .userNickname(userNickname)
                .rating(BigDecimal.valueOf(request.rating()))
                .content(request.content())
                .build();

            StoreReview savedReview = storeReviewRepository.save(review);

            // 3. 이미지가 있는 경우 ReviewImage 엔티티 생성
            if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
                List<ReviewImage> reviewImages = request.imageUrls().stream()
                    .map(imageUrl -> ReviewImage.builder()
                        .reviewId(savedReview.getId())
                        .imageUrl(imageUrl)
                        .sortOrder(1) // 기본 정렬 순서
                        .build())
                    .collect(Collectors.toList());

                reviewImageRepository.saveAll(reviewImages);
            }

            // 4. 포인트 획득 (리뷰 작성 시 10포인트)
            userPointService.earnPoints(userId, 10);

            // 5. DTO 변환
            StoreReviewsResponse.Review response = StoreReviewsResponse.Review.builder()
                .id(savedReview.getId())
                .userNickname(savedReview.getUserNickname())
                .rating(savedReview.getRating().doubleValue())
                .content(savedReview.getContent())
                .build();

            log.debug("리뷰 작성 완료 - reviewId: {}, 포인트 획득: 10", savedReview.getId());
            return response;

        } catch (IllegalArgumentException e) {
            throw e; // 재던짐 (Controller에서 적절한 HTTP 상태 코드 처리)
        } catch (StoreNotFoundException e) {
            throw e; // 재던짐 (Controller에서 적절한 HTTP 상태 코드 처리)
        } catch (Exception e) {
            log.error("리뷰 작성 중 예상치 못한 오류 - storeId: {}", request.storeId(), e);
            throw new StoreReviewServiceException("리뷰 작성 실패", e);
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
