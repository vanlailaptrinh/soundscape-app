package com.spotify.util;

import com.spotify.exception.TokenBlacklistedException;
import com.spotify.security.CustomUserDetails;
import com.spotify.service.auth.JwtBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Autowired
    JwtBlacklistService jwtBlacklistService;

    // Secret key must be Base64-encoded and long enough for security
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.accessTokenExpiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    // Generate the signing key from the secret key
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate a JWT token
    public String generateAccessToken(CustomUserDetails user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId())) // Store userId in the subject
                .claim("roles", user.getRoles()) // Store username in claims
                .setIssuedAt(new Date()) // Add issued time
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION)) // Set expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Use secure algorithm
                .compact();
    }

    // Extract userId from the token
    public Long extractUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject()); // Extract userId from subject
    }

    public boolean validateToken(String token) {
        if (jwtBlacklistService.isTokenBlacklisted(token)) {
            throw new TokenBlacklistedException("Token is blacklisted");
        }

        Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);

        return true;
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Lấy roles từ token
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public Date getExpirationFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Sửa lỗi ở đây
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

}