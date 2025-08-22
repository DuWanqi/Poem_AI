package com.example.poemai.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    private static final String SECRET_STRING = "your-secret-key-your-secret-key-1234-very-long-secret";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    private static final long EXPIRATION_TIME = 604800000; // 7天 (原为24小时)

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? 
               bearerToken.substring(7) : null;
    }

    public String generateToken(String username) {
        Date now = new Date();
        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
        
        System.out.println("生成Token for用户: " + username);
        System.out.println("Token前20字符: " + token.substring(0, Math.min(token.length(), 20)) + "...");
        return token;
    }

    public String extractUsername(String token) {
        try {
            Claims claims = parseToken(token);
            String username = claims.getSubject();
            System.out.println("从Token提取用户名: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("提取用户名失败: " + e.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            boolean isValid = expiration.after(new Date());
            System.out.println("Token验证结果: " + isValid + ", 过期时间: " + expiration);
            return isValid;
        } catch (ExpiredJwtException e) {
            System.out.println("Token已过期: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("Token格式错误: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Token验证异常: " + e.getMessage());
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}