package com.example.kakao_login.exception;

/**
 * 매장 상세 조회 서비스 예외
 * - 500 Internal Server Error용
 */
public class StoreDetailServiceException extends RuntimeException {
    
    public StoreDetailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public StoreDetailServiceException(String message) {
        super(message);
    }
}
