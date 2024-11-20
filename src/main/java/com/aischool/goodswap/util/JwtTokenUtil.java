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
            @Value("${jwt.key}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        logger.info("JwtTokenUtil initialized with access token expiration time: " + accessTokenExpirationTime +
                ", refresh token expiration time: " + refreshTokenExpirationTime);
    }

    // 토큰 생성 시 로그 추가
    public String generateAccessToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        logger.info("Generated access token for user: " + username); // 생성된 access token 로그
        logger.fine("Generated access token: " + token); // 생성된 token 값도 확인
        return token;
    }

    // 리프레시 토큰 생성 시 로그 추가
    public String generateRefreshToken(String username) {
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        logger.info("Generated refresh token for user: " + username); // 생성된 refresh token 로그
        logger.fine("Generated refresh token: " + refreshToken); // 생성된 refresh token 값 확인
        return refreshToken;
    }

    // 토큰에서 사용자명 추출 시 로그 추가
    public String extractUsername(String token) {
        try {
            System.out.println(token);
            Claims claims = getClaims(token);
            System.out.println(claims);
            String username = claims.getSubject();
            if (username == null) {
                logger.severe("Failed to extract username: null username found in token");
                throw new InvalidTokenException("토큰에서 사용자명을 추출할 수 없습니다.");
            }
            logger.info("Extracted username from token: " + username); // 추출된 username 로그
            return username;
        } catch (Exception e) {
            logger.severe("Failed to extract username from token: " + e.getMessage()); // 오류 로그
            throw new InvalidTokenException("유효하지 않은 토큰입니다: " + e.getMessage());
        }
    }

    // 토큰 검증 시 로그 추가
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            String type = (String) claims.get("type");
            if (!claims.getExpiration().after(new Date()) || !"access".equals(type)) {
                logger.severe("Invalid token type or expired token: " + type + ", Expiration: " + claims.getExpiration());
                throw new InvalidTokenException("유효하지 않거나 잘못된 타입의 토큰입니다.");
            }
            logger.info("Token is valid. Type: " + type); // 토큰이 유효할 때 로그
            return true;
        } catch (InvalidTokenException e) {
            logger.severe("Token validation failed: " + e.getMessage()); // 토큰 검증 실패 로그
            return false;
        }
    }

    // 클레임 추출 시 로그 추가
    private Claims getClaims(String token) {
        try {
            logger.fine("Parsing token to get claims: " + token); // 파싱 전 로그
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.severe("Failed to extract claims from token: " + e.getMessage()); // 클레임 추출 실패 로그
            throw new InvalidTokenException("토큰에서 클레임을 추출할 수 없습니다: " + e.getMessage());
        }
    }
}
