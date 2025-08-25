package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.menu.MenuBoardImageResponse;
import com.example.kakao_login.service.MenuBoardImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메뉴판 이미지 컨트롤러
 * - 매장별 메뉴판 이미지 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menu-board-images")
@RequiredArgsConstructor
public class MenuBoardImageController {

    private final MenuBoardImageService menuBoardImageService;

    /**
     * 매장의 메뉴판 이미지 목록 조회
     * @param storeId 매장 ID
     * @return 메뉴판 이미지 목록
     */
    @GetMapping("/stores/{storeId}")
    public ApiResponse<List<MenuBoardImageResponse>> getMenuBoardImages(@PathVariable String storeId) {
        log.info("메뉴판 이미지 조회 요청: storeId={}", storeId);
        
        List<MenuBoardImageResponse> menuBoardImages = menuBoardImageService.getMenuBoardImages(storeId);
        
        log.info("메뉴판 이미지 조회 완료: storeId={}, count={}", storeId, menuBoardImages.size());
        return ApiResponse.success(menuBoardImages);
    }
}
