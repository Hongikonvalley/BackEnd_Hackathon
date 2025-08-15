package com.example.kakao_login.common;

public record ApiResponse<T>(boolean isSuccess, int code, T result) {
    public static <T> ApiResponse<T> ok(T body) { return new ApiResponse<>(true, 200, body); }
}