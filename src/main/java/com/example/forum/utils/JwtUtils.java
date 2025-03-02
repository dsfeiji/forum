package com.example.forum.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Log log = LogFactory.get();
    private static final String TOKEN_TYPE = "JWT";
    private static final String ALGORITHM = "HS256";
    
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key getSigningKey() {
        byte[] keyBytes = Base64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(String email, Integer userId) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + expirationTime);

            return Jwts.builder()
                    .setHeaderParam("typ", TOKEN_TYPE)
                    .setHeaderParam("alg", ALGORITHM)
                    .setSubject(email)
                    .claim("userId", userId)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            log.error("Token生成失败: {}", e.getMessage());
            throw new JwtException("Token生成失败", e);
        }
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("Token已过期: {}", e.getMessage());
            throw new JwtException("Token已过期");
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Token格式错误: {}", e.getMessage());
            throw new JwtException("Token格式错误");
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new JwtException("Token解析失败");
        }
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            System.out.println(claims.get("userId"));
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            log.error("获取用户ID失败: {}", e.getMessage());
            throw new JwtException("获取用户ID失败");
        }
    }

    /**
     * 获取用户邮箱
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("获取用户邮箱失败: {}", e.getMessage());
            throw new JwtException("获取用户邮箱失败");
        }
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("username", String.class);
        } catch (Exception e) {
            log.error("获取用户名失败: {}", e.getMessage());
            throw new JwtException("获取用户名失败");
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String email = claims.getSubject();
            Integer userId = claims.get("userId", Integer.class);
            return generateToken(email, userId);
        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage());
            throw new JwtException("刷新Token失败");
        }
    }
}