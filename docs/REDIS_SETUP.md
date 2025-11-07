# Redis Integration for JWT Authentication

This document describes the Redis integration for enhanced JWT authentication with token storage and blacklisting.

## Overview

Redis is used for two critical authentication features:

1. **Refresh Token Storage**: Refresh tokens are stored in Redis with automatic TTL (Time To Live) expiration
2. **Access Token Blacklist**: Invalidated access tokens are blacklisted in Redis to prevent reuse after logout

## Architecture

### Token Flow

```
Login/Register
   ↓
Generate JWT Tokens (access + refresh)
   ↓
Store refresh token in Redis with 7-day TTL
   ↓
Return tokens to client

Refresh Token Request
   ↓
Validate refresh token exists in Redis
   ↓
Verify JWT signature and expiration
   ↓
Generate new access token
   ↓
Return new access token

Logout
   ↓
Blacklist access token in Redis (with remaining TTL)
   ↓
Delete refresh token from Redis
   ↓
Both tokens now invalid
```

### Redis Key Structure

#### Refresh Tokens

- **Key Pattern**: `refresh_token:{token}` → `userId (username)`
- **Reverse Mapping**: `user_refresh:{userId}` → `{token}`
- **TTL**: 7 days (604800000 ms)
- **Purpose**: Enable token refresh and prevent stolen token usage
- **Note**: `userId` is the user's username (name field), not email

#### Blacklisted Tokens

- **Key Pattern**: `blacklist:token:{token}` → `"blacklisted"`
- **TTL**: Matches token's remaining expiration time
- **Purpose**: Prevent access token reuse after logout

## Setup Instructions

### 1. Environment Configuration

Add Redis password to root `.env` file:

```bash
# .env (root directory)
PG_USERNAME=postgres
PG_PASSWORD=your_pg_password
PG_DB=store_dev_db
REDIS_PASSWORD=your_redis_password
```

Add Redis configuration to `store-backend/.env`:

```bash
# store-backend/.env
PG_DB_NAME=store_dev_db
PG_USERNAME=postgres
PG_PASSWORD=your_pg_password
JWT_SECRET=your_base64_jwt_secret

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

### 2. Start Redis with Docker Compose

From project root:

```bash
# Start both PostgreSQL and Redis
docker-compose up -d

# Verify Redis is running
docker ps | grep store-redis

# Check Redis logs
docker logs store-redis
```

### 3. Test Redis Connection

```bash
# Connect to Redis CLI
docker exec -it store-redis redis-cli -a your_redis_password

# Test commands
127.0.0.1:6379> PING
PONG

127.0.0.1:6379> INFO server
# Shows Redis server version and configuration

127.0.0.1:6379> exit
```

### 4. Start Store Backend

```bash
cd store-backend
../gradlew bootRun
```

The application will connect to Redis on startup and log:

```
INFO  c.c.store.config.RedisConfig - Redis connection established
```

## Redis Configuration

### Docker Compose Settings

```yaml
redis:
  image: redis:7-alpine
  container_name: store-redis
  restart: unless-stopped
  ports:
    - "6379:6379"
  command: redis-server --requirepass ${REDIS_PASSWORD} --maxmemory 256mb --maxmemory-policy allkeys-lru
  volumes:
    - redis_data:/data
  healthcheck:
    test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
    interval: 10s
    timeout: 3s
    retries: 3
```

**Key Settings**:

- **requirepass**: Password authentication required
- **maxmemory**: 256MB memory limit
- **maxmemory-policy**: `allkeys-lru` - Evict least recently used keys when memory limit reached
- **persistence**: Data persisted to `/data` volume

### Spring Boot Configuration

`application-local.yml`:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
      lettuce:
        pool:
          max-active: 10 # Maximum active connections
          max-idle: 5 # Maximum idle connections
          min-idle: 2 # Minimum idle connections
          max-wait: 2000ms # Maximum wait time for connection
```

## Service Architecture

### 1. RedisTokenService

Manages refresh tokens in Redis with automatic expiration.

**Key Methods**:

- `storeRefreshToken(token, username)`: Store refresh token with 7-day TTL (username is the user's name field)
- `validateRefreshToken(token)`: Validate token exists and return userId (username)
- `deleteRefreshToken(token)`: Remove refresh token (used in logout)
- `deleteAllUserRefreshTokens(username)`: Revoke all user's refresh tokens

**Example Usage**:

```java
// Store refresh token after login (using username/name as identifier)
redisTokenService.storeRefreshToken(refreshToken, user.getUsername());

// Validate during token refresh (returns username)
String username = redisTokenService.validateRefreshToken(refreshToken);
if (username == null) {
    throw new BadCredentialsException("Invalid or expired refresh token");
}

// Delete during logout
redisTokenService.deleteRefreshToken(refreshToken);
```

### 2. TokenBlacklistService

Manages blacklisted access tokens in Redis with automatic TTL.

**Key Methods**:

- `blacklistToken(token)`: Add token to blacklist with TTL matching token expiration
- `isBlacklisted(token)`: Check if token is blacklisted

**Example Usage**:

```java
// Blacklist access token during logout
tokenBlacklistService.blacklistToken(accessToken);

// Check during authentication filter
if (tokenBlacklistService.isBlacklisted(jwt)) {
    // Reject request
    return;
}
```

### 3. AuthenticationService Integration

Updated authentication flow with Redis:

**Login**:

```java
public LoginResponse login(LoginRequest request) {
    // Authenticate user
    Authentication authentication = authenticationManager.authenticate(...);
    User user = (User) authentication.getPrincipal();

    // Generate tokens
    String accessToken = jwtUtil.generateAccessToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    // Store refresh token in Redis with TTL (using username as key)
    redisTokenService.storeRefreshToken(refreshToken, user.getUsername());

    return new LoginResponse(accessToken, refreshToken, userResponse);
}
```

**Token Refresh**:

```java
public String refreshToken(String refreshToken) {
    // Validate refresh token exists in Redis
    String userId = redisTokenService.validateRefreshToken(refreshToken);
    if (userId == null) {
        throw new BadCredentialsException("Invalid or expired refresh token");
    }

    // Validate JWT signature and expiration
    String username = jwtUtil.extractUsername(refreshToken);
    User user = userRepository.findByName(username).orElseThrow(...);

    if (!jwtUtil.validateToken(refreshToken, user)) {
        throw new BadCredentialsException("Invalid refresh token");
    }

    // Generate new access token
    return jwtUtil.generateAccessToken(user);
}
```

**Logout**:

```java
public void logout(String accessToken, String refreshToken) {
    // Blacklist access token
    if (accessToken != null) {
        tokenBlacklistService.blacklistToken(accessToken);
    }

    // Delete refresh token from Redis
    if (refreshToken != null) {
        redisTokenService.deleteRefreshToken(refreshToken);
    }
}
```

## Monitoring Redis

### View Stored Keys

```bash
# Connect to Redis
docker exec -it store-redis redis-cli -a your_redis_password

# List all refresh tokens
127.0.0.1:6379> KEYS refresh_token:*

# List all blacklisted tokens
127.0.0.1:6379> KEYS blacklist:token:*

# Get token value and TTL
127.0.0.1:6379> GET refresh_token:eyJhbG...
127.0.0.1:6379> TTL refresh_token:eyJhbG...
```

### Monitor Memory Usage

```bash
127.0.0.1:6379> INFO memory
used_memory_human:2.51M
maxmemory_human:256M
maxmemory_policy:allkeys-lru
```

### Check Active Connections

```bash
127.0.0.1:6379> INFO clients
connected_clients:5
```

## Security Considerations

### Production Recommendations

1. **Strong Password**: Use a strong, randomly generated Redis password

   ```bash
   # Generate secure password
   openssl rand -base64 32
   ```

2. **Network Isolation**: Keep Redis on private network, not exposed to internet

   ```yaml
   # docker-compose.yml for production
   redis:
     networks:
       - backend-network # Private network
     # Remove or restrict ports exposure
   ```

3. **TLS/SSL**: Enable Redis TLS for encrypted communication

   ```yaml
   command: redis-server --requirepass ${REDIS_PASSWORD} --tls-port 6380 --tls-cert-file /path/to/cert --tls-key-file /path/to/key
   ```

4. **Persistence Configuration**: Configure RDB or AOF for data durability

   ```yaml
   command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes --appendfsync everysec
   ```

5. **Monitoring**: Set up Redis monitoring and alerting
   - Monitor memory usage and eviction rate
   - Track connection count and errors
   - Alert on high latency or unavailability

## Troubleshooting

### Redis Connection Refused

**Symptom**: `org.springframework.data.redis.RedisConnectionFailureException`

**Solutions**:

1. Verify Redis container is running: `docker ps | grep store-redis`
2. Check Redis logs: `docker logs store-redis`
3. Verify password in `.env` matches between root and store-backend
4. Test connection: `docker exec -it store-redis redis-cli -a your_password PING`

### Authentication Failed

**Symptom**: `NOAUTH Authentication required` or `ERR invalid password`

**Solutions**:

1. Verify `REDIS_PASSWORD` is set correctly in both `.env` files
2. Check password has no special characters requiring escaping
3. Restart Redis container: `docker-compose restart redis`

### Memory Issues

**Symptom**: `OOM command not allowed when used memory > 'maxmemory'`

**Solutions**:

1. Increase maxmemory: Update docker-compose.yml `--maxmemory 512mb`
2. Check memory usage: `docker exec -it store-redis redis-cli -a your_password INFO memory`
3. Verify eviction policy is set: `maxmemory-policy allkeys-lru`
4. Clear old data: Review TTL settings and cleanup expired keys

### Token Refresh Failing

**Symptom**: `Invalid or expired refresh token` despite valid JWT

**Solutions**:

1. Check refresh token exists in Redis:
   ```bash
   docker exec -it store-redis redis-cli -a your_password
   127.0.0.1:6379> EXISTS refresh_token:eyJhbG...
   ```
2. Verify TTL hasn't expired:
   ```bash
   127.0.0.1:6379> TTL refresh_token:eyJhbG...
   ```
3. Check Redis service is running and accessible
4. Review application logs for Redis connection errors

## Testing

### Unit Tests

Tests use embedded Redis for isolation:

```java
@SpringBootTest
@ActiveProfiles("test")
class AuthenticationServiceTest {
    // Tests automatically use embedded Redis
    // No manual Redis setup required
}
```

### Manual Testing with Postman

1. **Login** → Store access + refresh tokens in environment
2. **Verify Redis Storage**:
   ```bash
   docker exec -it store-redis redis-cli -a your_password
   127.0.0.1:6379> KEYS refresh_token:*
   127.0.0.1:6379> KEYS blacklist:*
   ```
3. **Logout** → Verify tokens are blacklisted/deleted
4. **Try Using Blacklisted Token** → Should return 401 Unauthorized

## Benefits of Redis Integration

### Performance

- **Fast In-Memory Operations**: Sub-millisecond token validation
- **Connection Pooling**: Reuse connections for high throughput
- **Scalability**: Distributed cache supports multiple backend instances

### Security

- **Automatic TTL**: Expired tokens automatically removed
- **Centralized Blacklist**: Consistent across all backend instances
- **Revocation Support**: Immediate token invalidation on logout

### Operational

- **No Manual Cleanup**: Redis automatically removes expired keys
- **Monitoring**: Built-in INFO commands for health checks
- **Persistence**: Optional data durability for critical tokens

## Migration from In-Memory to Redis

Previous implementation used `ConcurrentHashMap` for in-memory storage. Redis provides:

| Feature          | In-Memory (Old)         | Redis (New)                 |
| ---------------- | ----------------------- | --------------------------- |
| **Persistence**  | Lost on restart         | Configurable persistence    |
| **Distribution** | Single instance only    | Multi-instance support      |
| **Cleanup**      | Manual periodic cleanup | Automatic TTL expiration    |
| **Scalability**  | Limited by JVM memory   | Dedicated memory + eviction |
| **Monitoring**   | None                    | Built-in INFO commands      |

## Additional Resources

- [Redis Documentation](https://redis.io/documentation)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Redis Best Practices](https://redis.io/docs/management/optimization/)
- [Lettuce Redis Client](https://lettuce.io/)
