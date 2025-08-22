package com.example.kakao_login.exception;

/**
 * 리뷰 접근 권한이 없을 때 발생하는 예외
 */
public class ReviewAccessDeniedException extends RuntimeException {
    
    public ReviewAccessDeniedException(String reviewId, String userId) {
        super("리뷰에 대한 접근 권한이 없습니다. 리뷰 ID: " + reviewId + ", 사용자 ID: " + userId);
    }
    
    public ReviewAccessDeniedException(String reviewId, String userId, Throwable cause) {
        super("리뷰에 대한 접근 권한이 없습니다. 리뷰 ID: " + reviewId + ", 사용자 ID: " + userId, cause);
    }
}
