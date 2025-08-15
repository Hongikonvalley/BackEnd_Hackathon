package com.example.kakao_login.dto.search;

import org.springframework.util.StringUtils;

public record StoreSearchRequest(
        String q,
        String categoryId,
        String tagIds,         // "t1,t2"
        Double lat,
        Double lng,
        Double radiusKm,
        String time,           // "HH:mm"
        Integer dayOfWeek,     // 0~6, null이면 서버 현지 요일
        Boolean openNow,
        Boolean hasDeal,
        Double distanceMaxKm,
        String sort,           // distance|popularity|discount|rating|recent
        Integer page,
        Integer size,
        String userId          // 로그인 사용자 (is_favorite 계산용)
) {
    public int pageOrDefault(){ return page == null || page < 1 ? 1 : page; }
    public int sizeOrDefault(){ return size == null || size < 1 ? 20 : size; }
    public boolean hasKeyword(){ return StringUtils.hasText(q); }
}
