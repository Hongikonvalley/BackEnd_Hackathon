package com.example.kakao_login.service;

import com.example.kakao_login.dto.menu.MenuBoardImageResponse;
import com.example.kakao_login.entity.MenuBoardImage;
import com.example.kakao_login.entity.Store;
import com.example.kakao_login.exception.StoreNotFoundException;
import com.example.kakao_login.repository.MenuBoardImageRepository;
import com.example.kakao_login.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 메뉴판 이미지 서비스
 * - 매장별 메뉴판 이미지 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuBoardImageService {

    private final MenuBoardImageRepository menuBoardImageRepository;
    private final StoreRepository storeRepository;

    /**
     * 매장의 메뉴판 이미지 목록 조회
     * @param storeId 매장 ID
     * @return 메뉴판 이미지 목록
     */
    public List<MenuBoardImageResponse> getMenuBoardImages(String storeId) {
        // 매장 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("매장을 찾을 수 없습니다: " + storeId));

        // 메뉴판 이미지 조회
        List<MenuBoardImage> menuBoardImages = menuBoardImageRepository
                .findActiveByStoreIdOrderBySortOrder(storeId);

        // DTO 변환
        return menuBoardImages.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * MenuBoardImage 엔티티를 DTO로 변환
     */
    private MenuBoardImageResponse convertToResponse(MenuBoardImage menuBoardImage) {
        return MenuBoardImageResponse.builder()
                .id(menuBoardImage.getId())
                .imageUrl(menuBoardImage.getImageUrl())
                .sortOrder(menuBoardImage.getSortOrder())
                .build();
    }
}
