package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                .map(store -> Map.of(
                        "id", (Object) store.getId(),
                        "name", (Object) store.getName(),
                        "address", (Object) store.getAddress()
                ))
                .toList();
        return ApiResponse.success(stores);
    }
}
