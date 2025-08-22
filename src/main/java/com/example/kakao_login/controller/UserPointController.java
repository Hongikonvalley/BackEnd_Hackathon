package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.point.PointResponse;
import com.example.kakao_login.service.UserPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 포인트 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserPointController {

    private final UserPointService userPointService;

    /**
     * 사용자 포인트 잔액 조회
     */
    @GetMapping("/points")
    public ApiResponse<PointResponse.PointBalanceResponse> getPointBalance(
        @RequestParam("userId") String userId
    ) {
        log.debug("사용자 포인트 조회 요청 - userId: {}", userId);
        
        PointResponse.PointBalanceResponse response = userPointService.getPointBalance(userId);
        return ApiResponse.success(response);
    }
}
