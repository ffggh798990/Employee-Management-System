package com.itheima.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

/**
 * JWT 令牌操作工具类
 */
public class JwtUtils {

    // 固定密钥（与测试类一致）
    private static final String SECRET_KEY = "dGVzdEtleUZvckpXVFdpdGhTMjU2Qml0c0ZvclNlY3VyaXR5";

    // 令牌过期时间：12 小时（单位：毫秒）
    private static final long EXPIRATION_TIME = 12 * 60 * 60 * 1000;

    /**
     * 生成 JWT 令牌
     * @param claims 自定义信息（键值对）
     * @return 生成的令牌字符串
     */
    public static String generateJwt(Map<String, Object> claims) {
        String jwt = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 指定加密算法和密钥
                .addClaims(claims)                              // 添加自定义信息
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .compact();
        return jwt;
    }

    /**
     * 校验并解析 JWT 令牌
     * @param token 令牌字符串
     * @return 解析后的 Claims 对象（包含自定义信息）
     * @throws io.jsonwebtoken.JwtException 如果令牌无效或已过期
     */
    public static Claims parseJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)          // 指定密钥
                .parseClaimsJws(token)              // 解析令牌
                .getBody();                         // 获取自定义信息
        return claims;
    }
}