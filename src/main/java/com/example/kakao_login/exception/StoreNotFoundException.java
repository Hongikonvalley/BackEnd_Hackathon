package com.example.kakao_login.exception;

/**
 * 매장을 찾을 수 없음 예외
 * - 404 Not Found 에러용
 */
public class StoreNotFoundException extends RuntimeException {
    
    private final String storeId;

    public StoreNotFoundException(String storeId) {
        super("매장을 찾을 수 없습니다: " + storeId);
        this.storeId = storeId;
    }

    public String getStoreId() {
        return storeId;
    }
}
