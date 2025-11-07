package com.comp5348.store.service.auth;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis-based Token Service
 *
 * Manages refresh tokens in Redis with TTL for automatic expiration
 * Provides high-performance token storage with distributed cache support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKEN_PREFIX = "user_refresh:";

    /**
     * Store refresh token with TTL
     * Key format: refresh_token:{token} -> username
     * Also creates reverse mapping: user_refresh:{username} -> {token}
     */
    public void storeRefreshToken(String token, String username) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + token;
            String userTokenKey = USER_REFRESH_TOKEN_PREFIX + username;

            // Store token -> username mapping with TTL
            redisTemplate
                .opsForValue()
                .set(
                    tokenKey,
                    username,
                    refreshTokenExpiration,
                    TimeUnit.MILLISECONDS
                );

            // Store username -> token mapping for easy token lookup by user
            redisTemplate
                .opsForValue()
                .set(
                    userTokenKey,
                    token,
                    refreshTokenExpiration,
                    TimeUnit.MILLISECONDS
                );

            log.debug(
                "Stored refresh token for user: {} with TTL: {}ms",
                username,
                refreshTokenExpiration
            );
        } catch (Exception e) {
            log.error(
                "Failed to store refresh token in Redis: {}",
                e.getMessage(),
                e
            );
            throw new RuntimeException("Failed to store refresh token", e);
        }
    }

    /**
     * Validate refresh token
     * Returns username if token is valid and exists in Redis
     */
    public String validateRefreshToken(String token) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + token;
            String username = redisTemplate.opsForValue().get(tokenKey);

            if (username != null) {
                log.debug("Refresh token validated for user: {}", username);
                return username;
            } else {
                log.warn("Refresh token not found or expired");
                return null;
            }
        } catch (Exception e) {
            log.error(
                "Failed to validate refresh token: {}",
                e.getMessage(),
                e
            );
            return null;
        }
    }

    /**
     * Delete refresh token (used during logout)
     */
    public void deleteRefreshToken(String token) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + token;

            // Get username before deleting to remove reverse mapping
            String username = redisTemplate.opsForValue().get(tokenKey);

            // Delete token -> username mapping
            redisTemplate.delete(tokenKey);

            // Delete username -> token reverse mapping
            if (username != null) {
                String userTokenKey = USER_REFRESH_TOKEN_PREFIX + username;
                redisTemplate.delete(userTokenKey);
                log.debug("Deleted refresh token for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Failed to delete refresh token: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete all refresh tokens for a user (used when user changes password or security revocation)
     */
    public void deleteAllUserRefreshTokens(String username) {
        try {
            String userTokenKey = USER_REFRESH_TOKEN_PREFIX + username;

            // Get the token first
            String token = redisTemplate.opsForValue().get(userTokenKey);

            // Delete username -> token mapping
            redisTemplate.delete(userTokenKey);

            // Delete token -> username mapping if token exists
            if (token != null) {
                String tokenKey = REFRESH_TOKEN_PREFIX + token;
                redisTemplate.delete(tokenKey);
                log.info("Deleted all refresh tokens for user: {}", username);
            }
        } catch (Exception e) {
            log.error(
                "Failed to delete user refresh tokens: {}",
                e.getMessage(),
                e
            );
        }
    }

    /**
     * Check if refresh token exists in Redis
     */
    public boolean existsRefreshToken(String token) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
        } catch (Exception e) {
            log.error(
                "Failed to check refresh token existence: {}",
                e.getMessage(),
                e
            );
            return false;
        }
    }
}
