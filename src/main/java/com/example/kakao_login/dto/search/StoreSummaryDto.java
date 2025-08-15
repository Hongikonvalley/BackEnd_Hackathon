package com.example.kakao_login.dto.search;

import java.util.List;

public record StoreSummaryDto(
        String id,
        String name,
        String address,
        String repImageUrl,
        Double distanceKm,
        Rating rating,
        Boolean isOpenNow,
        String nextOpenTime,    // ISO8601 문자열 or null
        Boolean isFavorite,
        List<String> categories,
        List<String> tags,
        Earlybird earlybird
){
    public record Rating(Double avg, Integer count){}
    public record Earlybird(Boolean hasDeal, Integer bestDiscountPct, String dealId, String timeWindow){}
}
