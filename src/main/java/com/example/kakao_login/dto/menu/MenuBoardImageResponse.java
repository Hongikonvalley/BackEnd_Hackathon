package com.example.kakao_login.dto.menu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 메뉴판 이미지 응답 DTO
 * - 매장별 메뉴판 이미지 정보
 */
@Builder
public record MenuBoardImageResponse(
    String id, // 메뉴판 이미지 ID
    
    @JsonProperty("image_url")
    String imageUrl, // 이미지 URL
    
    @JsonProperty("sort_order")
    Integer sortOrder // 정렬 순서
) {
}
