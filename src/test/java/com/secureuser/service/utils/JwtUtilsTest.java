package com.secureuser.service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        String secret = "/4afOnYUAAMwSmcZIfAvJbMD695iFoRfce8oYRk5kjZMoLyrTltfOqWv46t4nKqElRBcFnRtNyEksF6jO4Ep2A==";
        ReflectionTestUtils.setField(jwtUtils, "secret", secret);
        jwtUtils.init();
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60);

        String token = jwtUtils.generateToken(
                Jwts.builder()
                        .subject("user123")
                        .id(UUID.randomUUID().toString())
                        .issuedAt(now)
                        .expiration(expiry)
                        .claim("role", "ADMIN")
        );

        assertNotNull(token);
        Claims claims = jwtUtils.getClaims(token);
        assertEquals("user123", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void validateToken_shouldReturnClaims_whenTokenIsValid() {
        String token = generateValidToken();
        Jws<Claims> result = jwtUtils.validateToken(token);

        assertNotNull(result);
        assertEquals("user123", result.getPayload().getSubject());
    }

    @Test
    void validateToken_shouldReturnNull_whenTokenIsInvalid() {
        String invalidToken = "this.is.not.a.jwt";
        assertNull(jwtUtils.validateToken(invalidToken));
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        String token = generateValidToken();
        assertTrue(jwtUtils.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        Date now = new Date();
        Date expired = new Date(now.getTime() - 10000);

        String token = jwtUtils.generateToken(
                Jwts.builder()
                        .subject("expiredUser")
                        .issuedAt(new Date(now.getTime() - 60000))
                        .expiration(expired)
        );

        assertFalse(jwtUtils.isTokenValid(token));
    }

    @Test
    void getClaims_shouldReturnNull_whenTokenIsInvalid() {
        String invalidToken = "invalid.token.structure";
        assertNull(jwtUtils.getClaims(invalidToken));
    }

    private String generateValidToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 60000);

        return jwtUtils.generateToken(
                Jwts.builder()
                        .subject("user123")
                        .id(UUID.randomUUID().toString())
                        .issuedAt(now)
                        .expiration(expiry)
                        .claim("role", "ADMIN")
        );
    }

    @Test
    void init_shouldThrowException_whenKeyIsTooShortForHS512() {
        JwtUtils shortKeyJwtUtils = new JwtUtils();
        String shortSecret = "too-short-key";

        ReflectionTestUtils.setField(shortKeyJwtUtils, "secret", shortSecret);

        assertThrows(WeakKeyException.class, shortKeyJwtUtils::init);
    }

    @Test
    void generateToken_shouldUseHS512Algorithm() {
        String token = generateValidToken();

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
        assertTrue(headerJson.contains("\"alg\":\"HS512\""));
    }
}