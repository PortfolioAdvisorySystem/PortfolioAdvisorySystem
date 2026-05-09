package com.backend.stockAllocation.authentication;

import com.backend.stockAllocation.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    //services implementation
    public String generateToken(AppUser user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role",         user.getRole().name());
        extraClaims.put("firstName",    user.getFirstName());
        extraClaims.put("userId",       user.getId());

        // Add subscriberId only for SUBSCRIBER role
        if (user.getSubscriberId() != null) {
            extraClaims.put("subscriberId", user.getSubscriberId());
        }

        return buildToken(extraClaims, user, jwtExpiration);
    }
    public String generateRefreshToken(AppUser user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }
    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())       // email as subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    //token validation

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //extract data
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Long extractSubscriberId(String token) {
        Object subId = extractAllClaims(token).get("subscriberId");
        return subId != null ? Long.valueOf(subId.toString()) : null;
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }



}
