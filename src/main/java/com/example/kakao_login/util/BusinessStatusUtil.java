package com.example.kakao_login.util;

import com.example.kakao_login.entity.BusinessStatus;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 매장 영업 상태 계산 유틸리티
 */
@Component
public class BusinessStatusUtil {

    /**
     * 현재 시간 기준으로 영업 상태 메시지를 반환
     * 
     * @param businessStatus 매장의 영업 상태
     * @return 영업 상태 메시지
     */
    public String getBusinessStatusMessage(BusinessStatus businessStatus) {
        if (businessStatus == null) {
            return "영업 정보 없음";
        }

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

    /**
     * 현재 시간 기준으로 영업 상태를 판단
     * 
     * @param businessStatus 매장의 영업 상태
     * @return 실제 영업 상태 메시지
     */
    public String getCurrentBusinessStatus(BusinessStatus businessStatus) {
        if (businessStatus == null) {
            return "영업 정보 없음";
        }

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
