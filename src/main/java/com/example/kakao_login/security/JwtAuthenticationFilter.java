package com.example.kakao_login.security;

import com.example.kakao_login.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 필터를 적용하지 않을 URL 패턴들
    private static final List<String> EXCLUDE_URL_PREFIXES = List.of(
            "/api/v1/search/",
            "/actuator/",
            "/auth/",
            "/kakao/",
            "/oauth2/",        // ✅ 추가
            "/login/oauth2/",  // ✅ 추가
            "/h2-console/"
    );

    // 공개 “정확 경로”(슬래시로 끝나지 않는 단건)
    private static final Set<String> EXCLUDE_EXACT_PATHS = Set.of(
            "/error",           // 스프링 에러 핸들러
            "/actuator/health",  // 혹시 모를 정확 경로 매칭(안전빵)
            "/actuator/info"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 1) 프리플라이트(OPTIONS)는 무조건 스킵
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2) contextPath 제거한 “정규화된 경로”로 비교
        String uri = request.getRequestURI();                 // 예) /app/api/...
        String ctx = request.getContextPath();                // 예) /app
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());               // -> /api/...
        }

        // 3) 정확 경로 화이트리스트
        if (EXCLUDE_EXACT_PATHS.contains(uri)) {
            return true;
        }

        // 4) 접두사(프리픽스) 화이트리스트
        for (String prefix : EXCLUDE_URL_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;                                  // 공개 경로면 필터 비적용
            }
        }

        // 5) 나머지는 필터 적용
        return false;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token  = (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;

        // 토큰이 없으면: 익명으로 계속 진행 (permitAll은 통과, 보호 경로는 EntryPoint가 401 처리)
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = JwtUtil.validateToken(token);
            String userId = claims.getSubject();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, java.util.Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // 만료/불량 등 어떤 예외라도: 컨텍스트만 비우고 계속 진행
            SecurityContextHolder.clearContext();
            // 여기서 response.setStatus(...) 쓰지 말 것!
        }

        filterChain.doFilter(request, response);
    }


    private boolean isPublic(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String method = req.getMethod();
        // 검색/필터는 익명 허용
        if ("GET".equals(method) && uri.startsWith("/api/v1/search/")) return true;
        // 헬스체크 등 추가
        if ("GET".equals(method) && uri.startsWith("/actuator/health")) return true;
        return false;
    }


    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
