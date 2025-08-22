package com.example.kakao_login.dto.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * 리뷰 작성 요청 DTO
 */
@Builder
public record ReviewCreateRequest(
    @JsonProperty("store_id")
    String storeId, // 매장 ID
    
    @JsonProperty("user_nickname")
    String userNickname, // 사용자 닉네임
    
    @JsonProperty("rating")
    Double rating, // 평점 (1.0-5.0)
    
    @JsonProperty("content")
    String content, // 리뷰 내용
    
    @JsonProperty("image_urls")
    List<String> imageUrls // 이미지 URL 리스트 (선택사항)
) {
    public ReviewCreateRequest {
        // 유효성 검사는 별도 메서드로 분리
    }

    /**
     * 유효성 검사
     * @throws IllegalArgumentException 유효하지 않은 경우
     */
    public void validate() {
        if (storeId == null || storeId.trim().isEmpty()) {
            throw new IllegalArgumentException("매장 ID는 필수입니다.");
        }
        if (userNickname == null || userNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 닉네임은 필수입니다.");
        }
        if (rating == null || rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("평점은 1.0에서 5.0 사이여야 합니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }
    }
}
