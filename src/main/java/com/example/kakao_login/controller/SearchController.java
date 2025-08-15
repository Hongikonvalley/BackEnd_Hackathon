package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.common.PageResult;
import com.example.kakao_login.dto.search.FilterMetaResponse;
import com.example.kakao_login.dto.search.StoreSearchRequest;
import com.example.kakao_login.dto.search.StoreSummaryDto;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final com.example.kakao_login.service.SearchService service;

    @GetMapping("/stores")
    public ApiResponse<PageResult<StoreSummaryDto>> searchStores(
            @RequestParam(required = false) String q,
            @RequestParam(name="category_id", required = false) String categoryId,
            @RequestParam(name="tag_ids", required = false) String tagIds,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(name="radius_km", required = false) Double radiusKm,
            @RequestParam(required = false)
            @Pattern(regexp = "^[0-2][0-9]:[0-5][0-9]$", message = "time must be HH:mm")
            String time,
            @RequestParam(name="day_of_week", required = false) Integer dayOfWeek,
            @RequestParam(name="open_now", required = false) Boolean openNow,
            @RequestParam(name="has_deal", required = false) Boolean hasDeal,
            @RequestParam(name="distance_max_km", required = false) Double distanceMaxKm,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestHeader(name="X-USER-ID", required = false) String userId // JWT 연동 전 임시
    ){
        var req = new StoreSearchRequest(q, categoryId, tagIds, lat, lng, radiusKm, time,
                dayOfWeek, openNow, hasDeal, distanceMaxKm, sort, page, size, userId);
        return ApiResponse.ok(service.searchStores(req));
    }

    @GetMapping("/filters")
    public ApiResponse<FilterMetaResponse> getFilters(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(name="radius_km", required = false) Double radiusKm,
            @RequestParam(required = false) String type
    ){
        return ApiResponse.ok(service.getFilterMeta(lat, lng, radiusKm, type));
    }
}
