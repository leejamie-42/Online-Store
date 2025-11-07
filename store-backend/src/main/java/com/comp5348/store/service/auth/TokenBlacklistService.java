package com.comp5348.store.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis-based Token Blacklist Service
 *
 * Manages invalidated tokens (blacklist) using Redis with automatic TTL
 * Provides distributed blacklist support for multi-instance deployments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret-key}")
    private String secretKey;

    private SecretKey signingKey;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Add token to blacklist with TTL matching token expiration
     * Redis automatically removes expired entries
     */
    public void blacklistToken(String token) {
        try {
            // Extract expiration date from token
            Date expirationDate = extractExpiration(token);
            Date now = new Date();

            // Only blacklist if token is not already expired
            if (expirationDate.after(now)) {
                String key = BLACKLIST_PREFIX + token;
                long ttlMillis = expirationDate.getTime() - now.getTime();

                // Store in Redis with TTL matching token expiration
                redisTemplate
                    .opsForValue()
                    .set(key, "blacklisted", ttlMillis, TimeUnit.MILLISECONDS);

                log.info("Token added to blacklist with TTL: {}ms", ttlMillis);
            } else {
                log.debug("Token already expired, not adding to blacklist");
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
            // Still add to blacklist for safety with default 24h TTL
            String key = BLACKLIST_PREFIX + token;
            redisTemplate
                .opsForValue()
                .set(key, "blacklisted", 24, TimeUnit.HOURS);
            log.warn(
                "Token blacklisted with default 24h TTL due to parsing error"
            );
        }
    }

    /**
     * Check if token is blacklisted
     * Redis automatically handles expired entries
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            boolean isBlacklisted = Boolean.TRUE.equals(
                redisTemplate.hasKey(key)
            );

            if (isBlacklisted) {
                log.debug("Token found in blacklist");
            }
            return isBlacklisted;
        } catch (Exception e) {
            log.error(
                "Failed to check token blacklist status: {}",
                e.getMessage(),
                e
            );
            // Fail-safe: treat as not blacklisted if Redis error
            return false;
        }
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getExpiration();
    }

    /**
     * Clear all blacklisted tokens (for testing purposes)
     * Use with caution in production
     */
    public void clearBlacklist() {
        try {
            redisTemplate
                .keys(BLACKLIST_PREFIX + "*")
                .forEach(redisTemplate::delete);
            log.warn("Blacklist cleared");
        } catch (Exception e) {
            log.error("Failed to clear blacklist: {}", e.getMessage(), e);
        }
    }
}
