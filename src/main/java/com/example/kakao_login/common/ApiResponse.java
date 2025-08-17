package com.example.kakao_login.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 API 응답 클래스
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Boolean isSuccess; // 성공여부
    private String message; // 메시지
    private Integer code; // 상태코드
    private T result; // 응답데이터

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", 200, data);
    }

    /**
     * 성공 응답 생성 (기존 호환성)
     */
    public static <T> ApiResponse<T> ok(T data) {
        return success(data);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "OK", 200, null);
    }

    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> fail(String message, Integer code) {
        return new ApiResponse<>(false, message, code, null);
    }

    /**
     * 실패 응답 생성 (기본 에러코드)
     */
    public static <T> ApiResponse<T> fail(String message) {
        return fail(message, 400);
    }
}