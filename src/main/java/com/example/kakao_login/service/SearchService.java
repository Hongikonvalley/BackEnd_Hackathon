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
        Map<String,Object> raw = repo.getFilterMeta(lat, lng, radiusKm, type);

        var categories = ((List<Map<String,Object>>) raw.get("categories")).stream()
                .map(m -> new FilterMetaResponse.Category(
                        (String)m.get("id"), (String)m.get("name"), (String)m.get("parent_id")))
                .toList();

        var tags = ((List<Map<String,Object>>) raw.get("tags")).stream()
                .map(m -> new FilterMetaResponse.Tag(
                        (String)m.get("id"), (String)m.get("name"), (String)m.get("type"),
                        (Integer)m.get("count")))
                .toList();

        var sortOptions = (List<String>) raw.get("sort_options");
        var timeSlots = (List<String>) raw.get("time_slots");
        return new FilterMetaResponse(categories, tags, sortOptions, timeSlots);
    }
}
