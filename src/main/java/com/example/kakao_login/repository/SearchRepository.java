package com.example.kakao_login.repository;

import com.example.kakao_login.common.PageResult;
import com.example.kakao_login.dto.search.StoreSearchRequest;
import com.example.kakao_login.dto.search.StoreSummaryDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class SearchRepository {

    private final NamedParameterJdbcTemplate jdbc;
    public SearchRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public PageResult<StoreSummaryDto> searchStores(StoreSearchRequest req) {
        var params = new MapSqlParameterSource();
        var where = new StringBuilder(" WHERE s.is_active = 1 ");

        // --- 키워드 필터: 비교/LIKE 양쪽 모두 동일 collation 강제 ---
        if (req.hasKeyword()) {
            String kw = Optional.ofNullable(req.q()).orElse("");
            params.addValue("kw", kw);
            where.append("""
               AND (
                 (CAST(:kw AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci = '' COLLATE utf8mb4_0900_ai_ci)
                 OR s.name        COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', CAST(:kw AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci, '%')
                 OR s.ai_recommendation COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', CAST(:kw AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci, '%')
                 OR EXISTS (
                      SELECT 1 FROM menu_items mi
                       WHERE mi.store_id = s.id
                         AND mi.name COLLATE utf8mb4_0900_ai_ci LIKE CONCAT('%', CAST(:kw AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci, '%')
                 )
               )
            """);
        }

        // --- 카테고리: '=' 비교도 collation 통일 ---
        if (req.categoryId() != null && !req.categoryId().isBlank()) {
            where.append("""
              AND (s.category_id COLLATE utf8mb4_0900_ai_ci
                   = CAST(:catId AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci)
            """);
            params.addValue("catId", req.categoryId());
        }

        if (req.tagIds() != null && !req.tagIds().isBlank()) {
            var ids = Arrays.stream(req.tagIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())   // ✅ 빈 스트링 제거
                    .toList();
            if (!ids.isEmpty()) {
                where.append("""
           AND EXISTS (SELECT 1 FROM store_tags st
                       WHERE st.store_id = s.id AND st.tag_id IN (:tagIds))
        """);
                params.addValue("tagIds", ids);
            }
        }

        // 위치/반경
        String distanceExpr = "NULL";
        if (req.lat() != null && req.lng() != null) {
            params.addValue("lat", req.lat());
            params.addValue("lng", req.lng());
            distanceExpr = """
             (6371 * acos(
               cos(radians(:lat)) * cos(radians(s.lat)) *
               cos(radians(s.lng) - radians(:lng)) +
               sin(radians(:lat)) * sin(radians(s.lat))
             ))
            """;
            if (req.radiusKm() != null) {
                where.append(" AND ").append(distanceExpr).append(" <= :radiusKm ");
                params.addValue("radiusKm", req.radiusKm());
            }
            if (req.distanceMaxKm() != null) {
                where.append(" AND ").append(distanceExpr).append(" <= :distanceMax ");
                params.addValue("distanceMax", req.distanceMaxKm());
            }
            // 거리 사용 시 좌표 없는 매장은 제외
            where.append(" AND s.lat IS NOT NULL AND s.lng IS NOT NULL ");
        }

        // 시간/영업 필터
        Integer dow = req.dayOfWeek();
        if (dow == null) dow = LocalDateTime.now(ZoneId.of("Asia/Seoul")).getDayOfWeek().getValue() % 7; // 일=0
        params.addValue("dow", dow);

        boolean checkOpenNow = Boolean.TRUE.equals(req.openNow());
        String time = req.time();
        if (checkOpenNow || (time != null && !time.isBlank())) {
            if (time == null || time.isBlank()) {
                time = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            params.addValue("t", time);

            where.append("""
              AND (
                EXISTS ( SELECT 1 FROM store_open_hour h
                         WHERE h.store_id = s.id AND h.day_of_week = :dow
                           AND (
                             (h.is_24h = 1)
                             OR (h.open_time <= h.close_time AND :t >= h.open_time AND :t < h.close_time)
                             OR (h.open_time > h.close_time AND (:t >= h.open_time OR :t < h.close_time))
                           )
                           AND NOT (:t >= IFNULL(h.break_start,'23:59') AND :t < IFNULL(h.break_end,'00:00'))
                )
                OR
                EXISTS ( SELECT 1 FROM store_open_hour hp
                         WHERE hp.store_id = s.id AND hp.day_of_week = ((:dow + 6) % 7)
                           AND hp.open_time > hp.close_time
                           AND :t < hp.close_time
                           AND NOT (:t >= IFNULL(hp.break_start,'23:59') AND :t < IFNULL(hp.break_end,'00:00'))
                )
              )
            """);
        }

        // 딜 존재 필터: status 비교도 collation 통일
        if (Boolean.TRUE.equals(req.hasDeal())) {
            where.append("""
              AND EXISTS (
                SELECT 1 FROM earlybird_deals d
                LEFT JOIN earlybird_inventory i ON i.deal_id = d.id AND i.quota_left > 0
                WHERE d.store_id = s.id
                  AND (d.status COLLATE utf8mb4_0900_ai_ci = 'ACTIVE' COLLATE utf8mb4_0900_ai_ci)
              )
            """);
        }

        // 정렬
        String orderBy;
        if (req.sort() == null || req.sort().isBlank()) {
            // 기본 정렬: 좌표 있으면 거리순, 없으면 이름순
            if (req.lat() != null && req.lng() != null) {
                orderBy = " ORDER BY (distance_km IS NULL) ASC, distance_km ASC ";
            } else {
                orderBy = " ORDER BY name COLLATE utf8mb4_0900_ai_ci ASC ";
            }
        } else {
            switch (req.sort()) {
                case "distance" ->
                        orderBy = " ORDER BY (distance_km IS NULL) ASC, distance_km ASC ";
                case "rating" ->
                        orderBy = " ORDER BY (rating_avg IS NULL) ASC, rating_avg DESC, rating_count DESC ";
                case "discount" ->
                        orderBy = " ORDER BY (best_discount_pct IS NULL) ASC, best_discount_pct DESC ";
                case "popularity" ->
                        orderBy = " ORDER BY (popularity_score IS NULL) ASC, popularity_score DESC ";
                case "recent" ->
                        orderBy = " ORDER BY name COLLATE utf8mb4_0900_ai_ci ASC ";
                default -> {
                    if (req.lat() != null && req.lng() != null) {
                        orderBy = " ORDER BY (distance_km IS NULL) ASC, distance_km ASC ";
                    } else {
                        orderBy = " ORDER BY name COLLATE utf8mb4_0900_ai_ci ASC ";
                    }
                }
            }
        }

        int page = req.pageOrDefault();
        int size = req.sizeOrDefault();
        int offset = (page - 1) * size;
        params.addValue("limit", size);
        params.addValue("offset", offset);
        params.addValue("uid", req.userId());

        String sql = """
          WITH base AS (
            SELECT
              s.id, s.name, s.address, s.rep_image_url,
              s.rating_avg, s.rating_count,
              %s AS distance_km,
              (SELECT MAX(CASE WHEN discount_value REGEXP '^[0-9]+$'
                               THEN CAST(discount_value AS SIGNED) ELSE 0 END)
                 FROM earlybird_deals d
                WHERE d.store_id = s.id
                  AND (d.status COLLATE utf8mb4_0900_ai_ci = 'ACTIVE' COLLATE utf8mb4_0900_ai_ci)
              ) AS best_discount_pct,
              (SELECT 1 FROM user_favorites f
                WHERE (f.user_id COLLATE utf8mb4_0900_ai_ci
                       = CAST(:uid AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci)
                  AND f.store_id = s.id
                LIMIT 1) AS fav_flag,
              (COALESCE(s.rating_count,0) * 1.0 +
               (SELECT COUNT(*) FROM user_favorites uf WHERE uf.store_id = s.id) * 2.0) AS popularity_score
            FROM stores s
            %s
          )
          SELECT *
          FROM base
          %s
          LIMIT :limit OFFSET :offset
        """.formatted(distanceExpr, where, orderBy);

        // 1) 기본 행 조회 (DTO로 바로 매핑하지 말고 Map으로 받아서 후처리)
        var rows = jdbc.query(sql, params, (rs, n) -> {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", rs.getString("id"));
            m.put("name", rs.getString("name"));
            m.put("address", rs.getString("address"));
            m.put("rep_image_url", rs.getString("rep_image_url"));

            Number nDistance  = (Number) rs.getObject("distance_km");
            Number nRatingAvg = (Number) rs.getObject("rating_avg");
            Number nRatingCnt = (Number) rs.getObject("rating_count");
            Number nBestDisc  = (Number) rs.getObject("best_discount_pct");

            m.put("distance_km", nDistance == null ? null : nDistance.doubleValue());
            m.put("rating_avg",  nRatingAvg == null ? null : nRatingAvg.doubleValue());
            m.put("rating_count",nRatingCnt == null ? null : nRatingCnt.intValue());
            m.put("best_discount_pct", nBestDisc == null ? null : nBestDisc.intValue());
            m.put("is_favorite", rs.getObject("fav_flag") != null);
            return m;
        });

        // store_id 수집
        var storeIds = rows.stream().map(r -> (String) r.get("id")).toList();
        Map<String, List<String>> tagsByStore = Map.of();
        Map<String, List<String>> catsByStore = Map.of();
        Map<String, List<String>> menusByStore = Map.of();

        if (!storeIds.isEmpty()) {
            var p2 = new MapSqlParameterSource().addValue("ids", storeIds);

            // 2) 태그 배치 조회
            var tagList = jdbc.query("""
                SELECT st.store_id, t.name
                  FROM store_tags st
                  JOIN tags t ON t.id = st.tag_id
                 WHERE st.store_id IN (:ids)
                 ORDER BY t.name
            """, p2, (rs, n) -> Map.of(
                    "store_id", rs.getString("store_id"),
                    "name", rs.getString("name")
            ));
            var tmpTags = new HashMap<String, List<String>>();
            for (var row : tagList) {
                String sid = (String) row.get("store_id");
                String name = (String) row.get("name");
                tmpTags.computeIfAbsent(sid, k -> new ArrayList<>()).add(name);
            }
            tagsByStore = tmpTags;

            // 3) 카테고리 배치 조회
            var catList = jdbc.query("""
                SELECT s.id AS store_id, c.name
                  FROM stores s
                  JOIN categories c ON c.id = s.category_id
                 WHERE s.id IN (:ids)
            """, p2, (rs, n) -> Map.of(
                    "store_id", rs.getString("store_id"),
                    "name", rs.getString("name")
            ));
            var tmpCats = new HashMap<String, List<String>>();
            for (var row : catList) {
                String sid = (String) row.get("store_id");
                String name = (String) row.get("name");
                tmpCats.computeIfAbsent(sid, k -> new ArrayList<>()).add(name);
            }
            catsByStore = tmpCats;


            // 4) 메뉴 배치 조회
            var menuList = jdbc.query("""
                SELECT mi.store_id, mi.name
                  FROM menu_items mi
                 WHERE mi.store_id IN (:ids)
                 ORDER BY mi.sort_order ASC, mi.created_at ASC
            """, p2, (rs, n) -> Map.of(
                    "store_id", rs.getString("store_id"),
                    "name", rs.getString("name")
            ));
            var tmpMenus = new HashMap<String, List<String>>();
            for (var row : menuList) {
                String sid = (String) row.get("store_id");
                String name = (String) row.get("name");
                tmpMenus.computeIfAbsent(sid, k -> new ArrayList<>()).add(name);
            }
            menusByStore = tmpMenus;
        }

        // 5) DTO로 조립
        var items = new ArrayList<StoreSummaryDto>();
        for (var r : rows) {
            String sid = (String) r.get("id");
            var categories = catsByStore.getOrDefault(sid, List.of());
            var tags = tagsByStore.getOrDefault(sid, List.of());
            var menus = menusByStore.getOrDefault(sid, List.of()).stream()
                    .limit(3)
                    .toList();

            items.add(new StoreSummaryDto(
                    sid,
                    (String) r.get("name"),
                    (String) r.get("address"),
                    (String) r.get("rep_image_url"),
                    (Double) r.get("distance_km"),
                    new StoreSummaryDto.Rating(
                            (Double) r.get("rating_avg"),
                            (Integer) r.get("rating_count")
                    ),
                    null, // is_open_now (필요 시 Service에서 계산)
                    null, // next_open_time
                    (Boolean) r.get("is_favorite"),
                    categories,
                    tags,
                    menus,
                    new StoreSummaryDto.Earlybird(
                            r.get("best_discount_pct") != null,
                            (Integer) r.get("best_discount_pct"),
                            null,
                            null
                    )
            ));
        }

        // total 계산 (같은 where 사용)
        String countSql = "SELECT COUNT(*) FROM stores s " + where;
        long total = jdbc.queryForObject(countSql, params, Long.class);
        boolean hasNext = (long) page * size < total;

        return new PageResult<>(items, page, size, total, hasNext);
    }

    public Map<String, Object> getFilterMeta(Double lat, Double lng, Double radiusKm, String type) {
        var result = new HashMap<String, Object>();

        // categories: 홈 화면용 기본 4개 카테고리(더보기 제외)
        var categories = new ArrayList<Map<String, Object>>();
        categories.add(Map.of("id", "cafe", "name", "카페"));
        categories.add(Map.of("id", "bakery", "name", "베이커리"));
        categories.add(Map.of("id", "brunch", "name", "브런치"));
        categories.add(Map.of("id", "salad", "name", "샐러드"));
        result.put("categories", categories);

        // tags
        var params = new MapSqlParameterSource();
        String tagSql = "SELECT t.id, t.name, t.type, 0 AS cnt FROM tags t";
        if (lat != null && lng != null && radiusKm != null) {
            tagSql = """
                SELECT t.id, t.name, t.type, COUNT(*) AS cnt
                  FROM tags t
                  JOIN store_tags st ON st.tag_id = t.id
                  JOIN stores s ON s.id = st.store_id
                 WHERE (6371 * acos(
                    cos(radians(:lat)) * cos(radians(s.lat)) *
                    cos(radians(s.lng) - radians(:lng)) +
                    sin(radians(:lat)) * sin(radians(s.lat))
                 )) <= :r
            """;
            params.addValue("lat", lat);
            params.addValue("lng", lng);
            params.addValue("r", radiusKm);
            if (type != null && !type.isBlank()) {
                tagSql += " AND t.type = :type ";
                params.addValue("type", type);
            }
            tagSql += " GROUP BY t.id, t.name, t.type ORDER BY cnt DESC LIMIT 50 ";
        }

        var tags = jdbc.query(tagSql, params, (rs, n) -> {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", rs.getString("id"));
            m.put("name", rs.getString("name"));
            m.put("type", rs.getString("type"));
            Object cntObj = rs.getObject("cnt");
            m.put("count", (cntObj == null) ? 0 : ((Number) cntObj).intValue());
            return m;
        });
        result.put("tags", tags);

        result.put("sort_options", List.of("distance","popularity","discount","rating","recent"));
        result.put("time_slots", List.of(
                "05:00-06:00",
                "06:00-07:00",
                "07:00-08:00",
                "08:00-09:00"
        ));
        return result;
    }
}
