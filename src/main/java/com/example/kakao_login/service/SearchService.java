package com.example.kakao_login.service;

import com.example.kakao_login.common.PageResult;
import com.example.kakao_login.dto.search.FilterMetaResponse;
import com.example.kakao_login.dto.search.StoreSearchRequest;
import com.example.kakao_login.dto.search.StoreSummaryDto;
import com.example.kakao_login.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final SearchRepository repo;
    public SearchService(SearchRepository repo) { this.repo = repo; }

    public PageResult<StoreSummaryDto> searchStores(StoreSearchRequest req) {
        // TODO: is_open_now / next_open_time 보강 로직(원하면 추가)
        return repo.searchStores(req);
    }

    public FilterMetaResponse getFilterMeta(Double lat, Double lng, Double radiusKm, String type) {
        Map<String, Object> raw = repo.getFilterMeta(lat, lng, radiusKm, type);

        // 각 요소는 조회 결과에 따라 존재하지 않을 수 있으므로 안전하게 처리한다.
        List<Map<String, Object>> categoryList =
                (List<Map<String, Object>>) raw.getOrDefault("categories", List.of());
        var categories = categoryList.stream()
                .map(m -> new FilterMetaResponse.Category(
                        (String) m.get("id"),
                        (String) m.get("name"),
                        (String) m.get("parent_id")))
                .toList();

        List<Map<String, Object>> tagList =
                (List<Map<String, Object>>) raw.getOrDefault("tags", List.of());
        var tags = tagList.stream()
                .map(m -> new FilterMetaResponse.Tag(
                        (String) m.get("id"),
                        (String) m.get("name"),
                        (String) m.get("type"),
                        ((Number) m.getOrDefault("count", 0)).intValue()))
                .toList();

        var sortOptions = (List<String>) raw.getOrDefault("sort_options", List.of());
        var timeSlots = (List<String>) raw.getOrDefault("time_slots", List.of());
        return new FilterMetaResponse(categories, tags, sortOptions, timeSlots);
    }
}
