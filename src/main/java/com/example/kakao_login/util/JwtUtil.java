package com.example.kakao_login.util;

import com.example.kakao_login.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "PhPUlr2s8aMBDlWNXqwo1PMImqPV9mM8";

    public static String createJwt(User user) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("nickname", user.getNickname())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000 * 60 * 60)) // 1시간
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey("PhPUlr2s8aMBDlWNXqwo1PMImqPV9mM8".getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();  // 파싱 성공하면 유효함
    }

}
