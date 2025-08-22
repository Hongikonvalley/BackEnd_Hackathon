package com.example.kakao_login.service;

import com.example.kakao_login.dto.point.PointResponse;
import com.example.kakao_login.entity.UserPoint;
import com.example.kakao_login.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 포인트 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointService {

    private final UserPointRepository userPointRepository;

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

        log.info("사용자 포인트 차감 - userId: {}, points: {}, before: {}, after: {}", 
            userId, points, beforeBalance, userPoint.getPointBalance());
    }
}
