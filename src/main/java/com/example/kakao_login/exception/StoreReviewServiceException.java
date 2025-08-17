package com.example.kakao_login.exception;

/**
 * 매장 리뷰 서비스 예외
 * - 500 Internal Server Error용
 */
public class StoreReviewServiceException extends RuntimeException {
    
    public StoreReviewServiceException(String message) {
        super(message);
    }

    public StoreReviewServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
