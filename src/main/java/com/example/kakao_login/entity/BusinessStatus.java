package com.example.kakao_login.entity;

/**
 * 매장 영업 상태 열거형
 */
public enum BusinessStatus {
    OPEN_24H("24시간 영업"),
    OPEN("영업중"),
    PREPARING("준비중"),
    CLOSED("영업종료"),
    BREAK_TIME("브레이크타임"),
    HOLIDAY("휴무일");

    private final String displayName; // 화면표시명

    BusinessStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
