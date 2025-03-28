package com.secureuser.service.service;

import com.secureuser.service.constants.JWTokenType;
import com.secureuser.service.dto.TokenObject;
import com.secureuser.service.model.Tokens;
import com.secureuser.service.model.Users;
import com.secureuser.service.proto.user.auth.AuthResponse;
import com.secureuser.service.repository.TokensRepository;
import com.secureuser.service.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RedisService redisService;

    @Mock
    private TokensRepository tokensRepository;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(tokenService, "projectName", "secure-user-service");
        ReflectionTestUtils.setField(tokenService, "expirationAccess", 3600L);
        ReflectionTestUtils.setField(tokenService, "expirationRefresh", 7200L);
    }

    @Test
    void generateToken_accessToken_shouldReturnTokenObjectAndSaveToRedisAndDb() {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setLogin("test-user");
        user.setTokens(new ArrayList<>());

        when(jwtUtils.generateToken(any())).thenReturn("generated-jwt-token");
        when(tokensRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UUID sessionId = UUID.randomUUID();

        TokenObject tokenObject = tokenService.generateToken(user, JWTokenType.ACCESS,sessionId);

        assertNotNull(tokenObject);
        assertEquals("generated-jwt-token", tokenObject.getToken());
        assertEquals(3600L, tokenObject.getLifeTime());

        verify(redisService).save(startsWith("jwt:ACCESS:"), eq("generated-jwt-token"), eq(3600L));
        verify(tokensRepository).save(any(Tokens.class));

        assertEquals(1, user.getTokens().size());
        assertEquals("ACCESS", user.getTokens().getFirst().getTokenType());
    }

    @Test
    void generateToken_refreshToken_shouldUseRefreshExpiration() {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setLogin("refresh-user");
        user.setTokens(new ArrayList<>());

        when(jwtUtils.generateToken(any())).thenReturn("refresh-token");
        when(tokensRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UUID sessionId = UUID.randomUUID();

        TokenObject tokenObject = tokenService.generateToken(user, JWTokenType.REFRESH, sessionId);

        assertNotNull(tokenObject);
        assertEquals("refresh-token", tokenObject.getToken());
        assertEquals(7200L, tokenObject.getLifeTime());

        verify(redisService).save(startsWith("jwt:REFRESH:"), eq("refresh-token"), eq(7200L));
    }

    @Test
    void validateToken_validToken_shouldReturnTrue() {
        when(jwtUtils.isTokenValid("valid-token")).thenReturn(true);

        boolean result = tokenService.validateToken("valid-token");

        assertTrue(result);
        verify(jwtUtils).isTokenValid("valid-token");
    }

    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        when(jwtUtils.isTokenValid("invalid-token")).thenReturn(false);

        boolean result = tokenService.validateToken("invalid-token");

        assertFalse(result);
        verify(jwtUtils).isTokenValid("invalid-token");
    }

    @Test
    void revokeAllUserTokens_shouldRevokeTokensAndRemoveFromRedisAndSaveAll() {
        Tokens token1 = new Tokens();
        token1.setToken("token1");
        token1.setTokenType("ACCESS");
        token1.setRevoked(false);

        Tokens token2 = new Tokens();
        token2.setToken("token2");
        token2.setTokenType("REFRESH");
        token2.setRevoked(false);

        Users user = new Users();
        user.setTokens(new ArrayList<>(List.of(token1, token2)));

        tokenService.revokeAllUserTokens(user);

        assertTrue(token1.getRevoked());
        assertTrue(token2.getRevoked());

        verify(redisService).delete("jwt:ACCESS:token1");
        verify(redisService).delete("jwt:REFRESH:token2");
        verify(tokensRepository).saveAll(anyList());
    }

    @Test
    void revokeAllUserTokens_emptyTokens_shouldDoNothing() {
        Users user = new Users();
        user.setTokens(new ArrayList<>());

        tokenService.revokeAllUserTokens(user);

        verify(redisService, never()).delete(any());
        verify(tokensRepository).saveAll(eq(List.of()));
    }

    @Test
    void generateKeyName_shouldFormatCorrectly() {
        String result = ReflectionTestUtils.invokeMethod(tokenService, "generateKeyName", "ACCESS", "abc123");
        assertEquals("jwt:ACCESS:abc123", result);
    }

    @Test
    void refreshToken_withValidRefreshToken_returnsNewTokens() {
        UUID sessionId = UUID.randomUUID();
        String oldRefreshToken = "valid.refresh.token";
        Users user = new Users();
        user.setLogin("testUser");
        user.setId(UUID.randomUUID());

        Tokens existingToken = new Tokens();
        existingToken.setRevoked(false);
        existingToken.setOwner(user);
        existingToken.setSessionId(sessionId);
        existingToken.setToken(oldRefreshToken);

        when(tokensRepository.findByToken(oldRefreshToken)).thenReturn(Optional.of(existingToken));
        when(jwtUtils.generateToken(any())).thenReturn("newAccessToken").thenReturn("newRefreshToken");

        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();
        tokenService.refreshToken(oldRefreshToken, responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getMessageCode());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void refreshToken_withRevokedToken_setsUnauthorizedError() {
        String oldRefreshToken = "revoked.refresh.token";
        Users user = new Users();
        Tokens revokedToken = new Tokens();
        revokedToken.setRevoked(true);
        revokedToken.setOwner(user);
        revokedToken.setToken(oldRefreshToken);

        when(tokensRepository.findByToken(oldRefreshToken)).thenReturn(Optional.of(revokedToken));

        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();
        tokenService.refreshToken(oldRefreshToken, responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(401, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getMessageCode());
        assertTrue(response.hasError());
        assertEquals("The token is no longer valid", response.getError().getErrorMessage());
    }

    @Test
    void refreshToken_withNonexistentToken_setsUnauthorizedError() {
        String token = "nonexistent.token";

        when(tokensRepository.findByToken(token)).thenReturn(Optional.empty());

        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();
        tokenService.refreshToken(token, responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(401, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getMessageCode());
        assertTrue(response.hasError());
    }
}