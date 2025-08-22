package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final StoreRepository storeRepository;

    @GetMapping("/stores")
    public ApiResponse<List<Map<String, Object>>> getStores() {
        List<Map<String, Object>> stores = storeRepository.findAll().stream()
                .map(store -> {
                    Map<String, Object> storeMap = new HashMap<>();
                    storeMap.put("id", store.getId());
                    storeMap.put("name", store.getName());
                    storeMap.put("address", store.getAddress());
                    storeMap.put("kakao_place_id", store.getKakaoPlaceId());
                    storeMap.put("naver_place_id", store.getNaverPlaceId());
                    return storeMap;
                })
                .toList();
        return ApiResponse.success(stores);
    }

    @PostMapping("/stores/gabiae/update-links")
    public ApiResponse<String> updateGabiaeLinks() {
        storeRepository.findByName("가비애").ifPresent(store -> {
            store.setKakaoPlaceId("20809319");
            store.setNaverPlaceId("1071920016");
            storeRepository.save(store);
        });
        return ApiResponse.success("가비애 매장 외부 링크 정보가 업데이트되었습니다.");
    }
}
