package com.example.kakao_login.dto.error;

import com.example.kakao_login.common.ApiResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 실무 기준 에러 응답 DTO
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @JsonProperty("error_code")
    String errorCode, // 에러코드 (클라이언트 구분용)
    
    String message, // 사용자용 메시지
    
    @JsonProperty("error_detail")
    String errorDetail, // 개발자용 상세 메시지
    
    @JsonProperty("trace_id")
    String traceId, // 추적ID (로깅용)
    
    String timestamp, // 발생시각
    
    String path, // 요청 경로
    
    @JsonProperty("validation_errors")
    List<ValidationError> validationErrors // 유효성 검증 에러들
) {

    /**
     * 유효성 검증 에러 정보
     */
    @Builder
    public record ValidationError(
        String field, // 필드명
        Object value, // 입력값
        String message // 에러메시지
    ) {}

    /**
     * 기본 에러 응답 빌더
     */
    public static class Factory {
        public static ErrorResponseBuilder create(ErrorCode errorCode, String path) {
            return ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(UUID.randomUUID().toString().substring(0, 8))
                .timestamp(LocalDateTime.now().toString())
                .path(path);
        }
    }

    /**
     * 에러 코드 열거형
     */
    public enum ErrorCode {
        // 비즈니스 에러
        STORE_NOT_FOUND("E001", "매장을 찾을 수 없습니다."),
        MENU_NOT_FOUND("E002", "메뉴를 찾을 수 없습니다."),
        DEAL_NOT_AVAILABLE("E003", "현재 이용할 수 없는 할인입니다."),
        
        // 인증/인가 에러
        UNAUTHORIZED("A001", "인증이 필요합니다."),
        FORBIDDEN("A002", "접근 권한이 없습니다."),
        TOKEN_EXPIRED("A003", "토큰이 만료되었습니다."),
        
        // 요청 에러
        INVALID_REQUEST("R001", "잘못된 요청입니다."),
        VALIDATION_FAILED("R002", "입력값 검증에 실패했습니다."),
        MISSING_PARAMETER("R003", "필수 파라미터가 누락되었습니다."),
        NOT_FOUND("R004", "요청한 자원을 찾을 수 없습니다."),
        
        // 서버 에러
        INTERNAL_SERVER_ERROR("S001", "서버 내부 오류가 발생했습니다."),
        DATABASE_ERROR("S002", "데이터베이스 오류가 발생했습니다."),
        EXTERNAL_API_ERROR("S003", "외부 API 호출 중 오류가 발생했습니다.");

        private final String code; // 에러코드
        private final String message; // 기본메시지

        ErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    /**
     * 매장을 찾을 수 없음 에러
     */
    public static ApiResponse<ErrorResponse> storeNotFound(String storeId, String path) {
        ErrorResponse error = Factory.create(ErrorCode.STORE_NOT_FOUND, path)
            .errorDetail("Store ID: " + storeId + " does not exist")
            .build();
        return ApiResponse.fail(error.message(), 404);
    }

    /**
     * 유효성 검증 실패 에러
     */
    public static ApiResponse<ErrorResponse> validationFailed(List<ValidationError> errors, String path) {
        ErrorResponse error = Factory.create(ErrorCode.VALIDATION_FAILED, path)
            .validationErrors(errors)
            .build();
        return ApiResponse.fail(error.message(), 400);
    }

    /**
     * 잘못된 요청 에러
     */
    public static ApiResponse<ErrorResponse> badRequest(String detail, String path) {
        ErrorResponse error = Factory.create(ErrorCode.INVALID_REQUEST, path)
            .errorDetail(detail)
            .build();
        return ApiResponse.fail(error.message(), 400);
    }

    /**
     * 인증 필요 에러
     */
    public static ApiResponse<ErrorResponse> unauthorized(String path) {
        ErrorResponse error = Factory.create(ErrorCode.UNAUTHORIZED, path)
            .build();
        return ApiResponse.fail(error.message(), 401);
    }

    /**
     * 권한 부족 에러
     */
    public static ApiResponse<ErrorResponse> forbidden(String path) {
        ErrorResponse error = Factory.create(ErrorCode.FORBIDDEN, path)
            .build();
        return ApiResponse.fail(error.message(), 403);
    }

    /**
     * 서버 내부 에러
     */
    public static ApiResponse<ErrorResponse> internalServerError(String detail, String path) {
        ErrorResponse error = Factory.create(ErrorCode.INTERNAL_SERVER_ERROR, path)
            .errorDetail(detail)
            .build();
        return ApiResponse.fail(error.message(), 500);
    }

    // ErrorResponse record 내부(기존 static 메서드들 옆)에 추가
    public static ApiResponse<ErrorResponse> error(int httpStatus, String message, String path) {
        // HTTP → ErrorCode 매핑 (NOT_FOUND 없으면 INVALID_REQUEST로 대체해도 됨)
        ErrorCode code = switch (httpStatus) {
            case 400 -> ErrorCode.INVALID_REQUEST;
            case 401 -> ErrorCode.UNAUTHORIZED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;           // ← enum에 없다면 하나 추가하세요
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };

        var builder = Factory.create(code, path);
        if (message != null && !message.isBlank()) {
            builder = builder.message(message);        // 기본 메시지 대신 커스텀 메시지
        }
        ErrorResponse err = builder.build();

        // 프로젝트 규격 유지: ApiResponse.fail(message, status)
        return ApiResponse.fail(err.message(), httpStatus);
    }

}