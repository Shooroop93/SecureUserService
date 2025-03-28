package com.secureuser.service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    private SecretKey signingKey;
    private JwtParser jwtParser;
    private final MacAlgorithm signatureAlgorithm = Jwts.SIG.HS512;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtParser = Jwts.parser().verifyWith(signingKey).build();
    }

    public String generateToken(JwtBuilder builder) {
        return builder
                .signWith(signingKey, signatureAlgorithm)
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        try {
            return jwtParser.parseSignedClaims(token);
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        Jws<Claims> jws = validateToken(token);
        return jws != null && !isTokenExpired(jws.getPayload());
    }

    public Claims getClaims(String token) {
        Jws<Claims> jws = validateToken(token);
        return jws != null ? jws.getPayload() : null;
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}