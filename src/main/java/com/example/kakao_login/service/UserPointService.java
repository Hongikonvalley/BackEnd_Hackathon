package com.example.kakao_login.service;

import com.example.kakao_login.dto.point.PointResponse;
import com.example.kakao_login.dto.point.PointHistoryResponse;
import com.example.kakao_login.entity.UserPoint;
import com.example.kakao_login.entity.PointHistory;
import com.example.kakao_login.repository.UserPointRepository;
import com.example.kakao_login.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 포인트 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 사용자 포인트 잔액 조회
     * @param userId 사용자 ID
     * @return 포인트 잔액 응답
     */
    @Transactional(readOnly = true)
    public PointResponse.PointBalanceResponse getPointBalance(String userId) {
        if (userId == null) {
            return PointResponse.PointBalanceResponse.builder()
                .userId(null)
                .pointBalance(0)
                .build();
        }

        Integer pointBalance = userPointRepository.findPointBalanceByUserId(userId);
        if (pointBalance == null) {
            pointBalance = 0;
        }

        return PointResponse.PointBalanceResponse.builder()
            .userId(userId)
            .pointBalance(pointBalance)
            .build();
    }

    /**
     * 포인트 획득 (리뷰 작성 시)
     * @param userId 사용자 ID
     * @param points 획득할 포인트
     */
    @Transactional
    public void earnPoints(String userId, int points) {
        if (userId == null || points <= 0) {
            return;
        }

        UserPoint userPoint = userPointRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserPoint newPoint = UserPoint.create(userId);
                return userPointRepository.save(newPoint);
            });

        userPoint.earnPoints(points);
        userPointRepository.save(userPoint);

        log.info("사용자 포인트 획득 - userId: {}, points: {}, balance: {}", 
            userId, points, userPoint.getPointBalance());
    }

    /**
     * 포인트 차감 (리뷰 삭제 시)
     * @param userId 사용자 ID
     * @param points 차감할 포인트
     */
    @Transactional
    public void spendPoints(String userId, int points) {
        if (userId == null || points <= 0) {
            return;
        }

        UserPoint userPoint = userPointRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserPoint newPoint = UserPoint.create(userId);
                return userPointRepository.save(newPoint);
            });

        int beforeBalance = userPoint.getPointBalance();
        userPoint.spendPoints(points);
        userPointRepository.save(userPoint);

        // 포인트 사용 내역 저장
        PointHistory spendHistory = PointHistory.createSpendHistory(userId, points, "리뷰 삭제");
        pointHistoryRepository.save(spendHistory);

        log.info("사용자 포인트 차감 - userId: {}, points: {}, before: {}, after: {}", 
            userId, points, beforeBalance, userPoint.getPointBalance());
    }

    /**
     * 사용자 포인트 적립 내역 조회
     * @param userId 사용자 ID
     * @return 포인트 적립 내역 응답
     */
    @Transactional(readOnly = true)
    public PointHistoryResponse.PointHistoryListResponse getPointHistory(String userId) {
        if (userId == null) {
            return PointHistoryResponse.PointHistoryListResponse.builder()
                .userId(null)
                .pointBalance(0)
                .totalEarned(0)
                .totalSpent(0)
                .history(List.of())
                .build();
        }

        try {
            // 사용자 포인트 정보 조회
            UserPoint userPoint = userPointRepository.findByUserId(userId)
                .orElseGet(() -> UserPoint.create(userId));

            // 포인트 적립 내역 조회 (테이블이 없을 경우 빈 리스트 반환)
            List<PointHistory> histories = List.of();
            try {
                // 테이블 존재 여부 확인
                long tableExists = pointHistoryRepository.checkTableExists();
                if (tableExists > 0) {
                    histories = pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
                } else {
                    log.info("포인트 적립 내역 테이블이 존재하지 않습니다. 빈 내역을 반환합니다. - userId: {}", userId);
                }
            } catch (Exception e) {
                log.warn("포인트 적립 내역 조회 중 오류가 발생했습니다. 빈 내역을 반환합니다. - userId: {}, error: {}", userId, e.getMessage());
            }

            // DTO 변환
            List<PointHistoryResponse.PointHistoryItem> historyItems = histories.stream()
                .map(history -> PointHistoryResponse.PointHistoryItem.builder()
                    .id(history.getId())
                    .type(history.getType().name())
                    .points(history.getPoints())
                    .reason(history.getReason())
                    .createdAt(history.getCreatedAt())
                    .build())
                .collect(Collectors.toList());

            return PointHistoryResponse.PointHistoryListResponse.builder()
                .userId(userId)
                .pointBalance(userPoint.getPointBalance())
                .totalEarned(userPoint.getTotalEarned())
                .totalSpent(userPoint.getTotalSpent())
                .history(historyItems)
                .build();
        } catch (Exception e) {
            log.error("포인트 적립 내역 조회 중 오류 발생 - userId: {}", userId, e);
            return PointHistoryResponse.PointHistoryListResponse.builder()
                .userId(userId)
                .pointBalance(0)
                .totalEarned(0)
                .totalSpent(0)
                .history(List.of())
                .build();
        }
    }

    /**
     * 리뷰 등록 시 포인트 적립 (규칙 적용)
     * @param userId 사용자 ID
     */
    @Transactional
    public void earnPointsForReview(String userId) {
        if (userId == null) {
            return;
        }

        // 기존 리뷰 등록 횟수 조회
        long reviewCount = pointHistoryRepository.countReviewRegistrations(userId);
        long newReviewCount = reviewCount + 1;

        // 포인트 적립 규칙 적용
        int pointsToEarn = 10; // 기본 10포인트
        String reason = "리뷰 등록";

        // 특별 보너스 포인트 적용
        if (newReviewCount == 10) {
            pointsToEarn += 50;
            reason = "10번째 리뷰 등록";
        } else if (newReviewCount == 20) {
            pointsToEarn += 100;
            reason = "20번째 리뷰 등록";
        } else if (newReviewCount == 30) {
            pointsToEarn += 50;
            reason = "30번째 리뷰 등록";
        } else if (newReviewCount == 40) {
            pointsToEarn += 50;
            reason = "40번째 리뷰 등록";
        }

        // 포인트 적립
        earnPoints(userId, pointsToEarn);

        // 포인트 적립 내역 저장
        PointHistory earnHistory = PointHistory.createEarnHistory(userId, pointsToEarn, reason);
        pointHistoryRepository.save(earnHistory);

        log.info("리뷰 등록 포인트 적립 - userId: {}, reviewCount: {}, points: {}, reason: {}", 
            userId, newReviewCount, pointsToEarn, reason);
    }
}
