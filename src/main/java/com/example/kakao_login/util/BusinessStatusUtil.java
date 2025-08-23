package com.example.kakao_login.util;

import com.example.kakao_login.entity.BusinessStatus;
import com.example.kakao_login.entity.Store;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 매장 영업 상태 계산 유틸리티
 */
@Component
public class BusinessStatusUtil {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 현재 시간 기준으로 영업 상태 메시지를 반환
     * 
     * @param store 매장 정보
     * @return 영업 상태 메시지
     */
    public String getCurrentBusinessStatus(Store store) {
        if (store == null) {
            return "영업 정보 없음";
        }

        BusinessStatus businessStatus = store.getBusinessStatus();
        if (businessStatus == null) {
            return "영업 정보 없음";
        }

        // 24시간 영업인 경우
        if (businessStatus == BusinessStatus.OPEN_24H) {
            return "24시간 영업";
        }

        // 영업 시간이 설정되지 않은 경우 기본 상태 반환
        if (store.getOpenTime() == null || store.getCloseTime() == null) {
            return getDefaultBusinessStatusMessage(businessStatus);
        }

        // 현재 시간과 영업 시간 비교
        LocalTime currentTime = LocalTime.now();
        LocalTime openTime = parseTime(store.getOpenTime());
        LocalTime closeTime = parseTime(store.getCloseTime());

        if (openTime == null || closeTime == null) {
            return getDefaultBusinessStatusMessage(businessStatus);
        }

        // 24시간을 넘어가는 영업 (예: 22:00 ~ 06:00 또는 09:00 ~ 00:00)
        if (closeTime.isBefore(openTime)) {
            if (currentTime.isAfter(openTime) && currentTime.isBefore(closeTime)) {
                return "지금 영업중";
            } else if (currentTime.isBefore(openTime)) {
                return openTime.format(TIME_FORMATTER) + " 오픈";
            } else {
                return "영업종료";
            }
        } else {
            // 일반적인 영업 시간 (예: 09:00 ~ 22:00)
            if (currentTime.isAfter(openTime) && currentTime.isBefore(closeTime)) {
                return "지금 영업중";
            } else if (currentTime.isBefore(openTime)) {
                return openTime.format(TIME_FORMATTER) + " 오픈";
            } else {
                return "영업종료";
            }
        }
    }

    /**
     * 시간 문자열을 LocalTime으로 파싱
     */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 기본 영업 상태 메시지 반환
     */
    private String getDefaultBusinessStatusMessage(BusinessStatus businessStatus) {
        switch (businessStatus) {
            case OPEN_24H:
                return "24시간 영업";
            case OPEN:
                return "지금 영업중";
            case PREPARING:
                return "준비중";
            case CLOSED:
                return "영업종료";
            case BREAK_TIME:
                return "브레이크타임";
            case HOLIDAY:
                return "휴무일";
            default:
                return "영업 정보 없음";
        }
    }
}
