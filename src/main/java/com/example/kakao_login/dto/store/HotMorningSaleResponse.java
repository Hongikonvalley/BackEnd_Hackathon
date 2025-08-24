package com.example.kakao_login.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 오늘의 HOT 모닝 세일 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotMorningSaleResponse {
    
    /**
     * HOT 모닝 세일 매장 목록
     */
    private List<HotMorningSaleStoreDto> stores;
    
    /**
     * 전체 매장 수
     */
    private int totalCount;
    
    /**
     * HOT 모닝 세일 매장 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotMorningSaleStoreDto {
        
        /**
         * 매장 ID
         */
        private String storeId;
        
        /**
         * 매장명
         */
        private String storeName;
        
        /**
         * 대표 이미지 URL
         */
        private String repImageUrl;
        
        /**
         * 화면 표시 텍스트 (할인 정보)
         */
        private String displayText;
    }
}
