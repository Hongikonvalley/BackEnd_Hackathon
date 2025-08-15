package com.example.kakao_login.dto.search;

import java.util.List;

public record FilterMetaResponse(
        List<Category> categories,
        List<Tag> tags,
        List<String> sortOptions,
        List<String> timeSlots
) {
    public record Category(String id, String name, String parentId) {}
    public record Tag(String id, String name, String type, Integer count) {}
}