package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.favorite.FavoriteResponse;
import com.example.kakao_login.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 즐겨찾기 Controller
 */
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    /**
     * 즐겨찾기 추가
     */
    @PostMapping("/{storeId}/favorite")
    public ApiResponse<FavoriteResponse.ToggleResponse> addFavorite(
        @PathVariable String storeId,
        @RequestHeader("X-User-Id") String userId
    ) {
        userFavoriteService.addFavorite(userId, storeId);
        
        FavoriteResponse.ToggleResponse response = FavoriteResponse.ToggleResponse.builder()
            .isFavorite(true)
            .message("즐겨찾기에 추가되었습니다")
            .build();
            
        return ApiResponse.success(response);
    }

    /**
     * 즐겨찾기 삭제
     */
    @DeleteMapping("/{storeId}/favorite")
    public ApiResponse<FavoriteResponse.ToggleResponse> removeFavorite(
        @PathVariable String storeId,
        @RequestHeader("X-User-Id") String userId
    ) {
        userFavoriteService.removeFavorite(userId, storeId);
        
        FavoriteResponse.ToggleResponse response = FavoriteResponse.ToggleResponse.builder()
            .isFavorite(false)
            .message("즐겨찾기에서 삭제되었습니다")
            .build();
            
        return ApiResponse.success(response);
    }

    /**
     * 즐겨찾기한 매장 목록 조회
     */
    @GetMapping("/favorites")
    public ApiResponse<FavoriteResponse.StoreListResponse> getFavoriteStores(
        @RequestHeader("X-User-Id") String userId
    ) {
        FavoriteResponse.StoreListResponse response = userFavoriteService.getFavoriteStores(userId);
        return ApiResponse.success(response);
    }
}
