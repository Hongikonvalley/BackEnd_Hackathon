package com.example.kakao_login.exception;

/**
 * 리뷰를 찾을 수 없을 때 발생하는 예외
 */
public class ReviewNotFoundException extends RuntimeException {
    
    public ReviewNotFoundException(String reviewId) {
        super("리뷰를 찾을 수 없습니다: " + reviewId);
    }
    
    public ReviewNotFoundException(String reviewId, Throwable cause) {
        super("리뷰를 찾을 수 없습니다: " + reviewId, cause);
    }
}
