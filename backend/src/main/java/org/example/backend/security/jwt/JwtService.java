package org.example.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.model.entity.User;
import org.example.backend.repository.InvalidatedTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public JwtService(InvalidatedTokenRepository invalidatedTokenRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    public String generateToken(Map<String, Object> extraClaims, User user) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        if (user.getRole() != null) {
            claims.put("role", user.getRole().name());
        }

        String jti = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .id(jti)
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Instant extractExpirationInstant(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null ? expiration.toInstant() : Instant.now().plusMillis(jwtExpirationMs);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            String jti = claims.getId();
            if (jti != null && invalidatedTokenRepository != null && invalidatedTokenRepository.existsById(jti)) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT không hợp lệ: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean usernameMatches = username != null && (username.equals(userDetails.getUsername())
                    || (userDetails instanceof User u && (username.equals(u.getUsername()) || username.equals(u.getEmail()))));
            return usernameMatches && validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

