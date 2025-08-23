package com.example.kakao_login.dto.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * 리뷰 수정 요청 DTO
 */
@Builder
public record ReviewUpdateRequest(
    @JsonProperty("rating")
    Double rating, // 평점 (1.0-5.0)
    
    @JsonProperty("content")
    String content // 리뷰 내용
) {
    public ReviewUpdateRequest {
        if (rating != null && (rating < 1.0 || rating > 5.0)) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0: " + rating);
        }
        if (content != null && content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
    }
}
