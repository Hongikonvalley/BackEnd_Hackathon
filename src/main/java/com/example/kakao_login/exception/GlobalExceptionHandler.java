package com.example.kakao_login.exception;

import com.example.kakao_login.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 매장을 찾을 수 없음 예외 처리 (404)
     */
    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<?> handleStoreNotFound(
        StoreNotFoundException e, 
        HttpServletRequest request
    ) {
        log.warn("매장을 찾을 수 없음 - storeId: {}, path: {}", 
            e.getStoreId(), request.getRequestURI());
            
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.storeNotFound(e.getStoreId(), request.getRequestURI()));
    }

    /**
     * 매장 상세 서비스 예외 처리 (500)
     */
    @ExceptionHandler(StoreDetailServiceException.class)
    public ResponseEntity<?> handleStoreDetailServiceException(
        StoreDetailServiceException e, 
        HttpServletRequest request
    ) {
        log.error("매장 상세 서비스 오류 - path: {}", request.getRequestURI(), e);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.internalServerError(e.getMessage(), request.getRequestURI()));
    }

    /**
     * 매장 리뷰 서비스 예외 처리 (500)
     */
    @ExceptionHandler(StoreReviewServiceException.class)
    public ResponseEntity<?> handleStoreReviewServiceException(
        StoreReviewServiceException e, 
        HttpServletRequest request
    ) {
        log.error("매장 리뷰 서비스 오류 - path: {}", request.getRequestURI(), e);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.internalServerError(e.getMessage(), request.getRequestURI()));
    }

    /**
     * 유효성 검증 실패 예외 처리 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
        MethodArgumentNotValidException e, 
        HttpServletRequest request
    ) {
        log.warn("유효성 검증 실패 - path: {}, errors: {}", 
            request.getRequestURI(), e.getBindingResult().getFieldErrors());
            
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.badRequest("입력값 검증에 실패했습니다.", request.getRequestURI()));
    }

    /**
     * 제약 조건 위반 예외 처리 (400)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(
        ConstraintViolationException e, 
        HttpServletRequest request
    ) {
        log.warn("제약 조건 위반 - path: {}, violations: {}", 
            request.getRequestURI(), e.getConstraintViolations());
            
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.badRequest("입력값이 올바르지 않습니다.", request.getRequestURI()));
    }

    /**
     * 타입 변환 실패 예외 처리 (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(
        MethodArgumentTypeMismatchException e, 
        HttpServletRequest request
    ) {
        log.warn("타입 변환 실패 - path: {}, parameter: {}, value: {}", 
            request.getRequestURI(), e.getName(), e.getValue());
            
        String message = String.format("파라미터 '%s'의 값이 올바르지 않습니다.", e.getName());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.badRequest(message, request.getRequestURI()));
    }

    /**
     * 기타 예상치 못한 예외 처리 (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(
        Exception e, 
        HttpServletRequest request
    ) {
        log.error("예상치 못한 오류 발생 - path: {}", request.getRequestURI(), e);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.internalServerError("서버 내부 오류가 발생했습니다.", request.getRequestURI()));
    }
}
