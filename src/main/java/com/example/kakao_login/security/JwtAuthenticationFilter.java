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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 필터를 적용하지 않을 URL 패턴들
    private static final List<String> EXCLUDE_URLS = List.of(
            "/auth/",
            "/api/auth/",
            "/h2-console/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // OPTIONS (Preflight) 요청도 스킵
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // EXCLUDE_URLS 로 시작하면 필터 동작하지 않음
        return EXCLUDE_URLS.stream().anyMatch(path::startsWith);
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

        // 1) 토큰이 없으면 → 익명으로 통과 (permitAll 경로는 그대로 통과됨)
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

            filterChain.doFilter(request, response);
            return;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 2) 토큰 만료: 공개 경로면 통과, 아니면 401
            if (isPublic(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token expired");
            return;

        } catch (Exception e) {
            // 3) 토큰 불량: 공개 경로면 통과, 아니면 401(혹은 403)
            if (isPublic(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }
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
