package com.example.kakao_login.common;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long total, boolean hasNext) {}
