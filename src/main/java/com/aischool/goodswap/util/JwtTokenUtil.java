package com.aischool.goodswap.util;

import com.aischool.goodswap.exception.auth.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class JwtTokenUtil {

    private final Key key;
    @Getter
    private final long accessTokenExpirationTime;
    @Getter
    private final long refreshTokenExpirationTime;
    private static final Logger logger = Logger.getLogger(JwtTokenUtil.class.getName());

    @Autowired
    public JwtTokenUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        logger.info("Generated refresh token for user: " + username);
        return refreshToken;
    }

    public String extractUsername(String token) {
        try {
            Claims claims = getClaims(token);
            String username = claims.getSubject();
            if (username == null) {
                throw new InvalidTokenException("토큰에서 사용자명을 추출할 수 없습니다.");
            }
            logger.info("Extracted username from token: " + username);
            return username;
        } catch (Exception e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            String type = (String) claims.get("type");
            if (!claims.getExpiration().after(new Date()) || !"access".equals(type)) {
                throw new InvalidTokenException("유효하지 않거나 잘못된 타입의 토큰입니다.");
            }
            return true;
        } catch (InvalidTokenException e) {
            logger.severe("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new InvalidTokenException("토큰에서 클레임을 추출할 수 없습니다: " + e.getMessage());
        }
    }
}
