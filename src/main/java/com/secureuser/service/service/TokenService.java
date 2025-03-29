package com.secureuser.service.service;

import com.secureuser.service.constants.JWTokenType;
import com.secureuser.service.dto.TokenObject;
import com.secureuser.service.model.Tokens;
import com.secureuser.service.model.Users;
import com.secureuser.service.proto.user.auth.AuthResponse;
import com.secureuser.service.proto.user.auth.LogoutRequest;
import com.secureuser.service.repository.TokensRepository;
import com.secureuser.service.utils.JwtUtils;
import com.secureuser.service.utils.TimeUtils;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.secureuser.service.utils.GRPCHelperMessage.formulateAResponse;
import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtils;
    private final RedisService redisService;
    private final TokensRepository tokensRepository;

    @Value("${spring.application.name}")
    private String projectName;

    @Value("${spring.security.jwt.expiration.access}")
    private long expirationAccess;

    @Value("${spring.security.jwt.expiration.refresh}")
    private long expirationRefresh;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TokenObject generateToken(Users user, JWTokenType tokenType, UUID sessionId) {
        log.info("Start generating JWT token for user: {}", user.getLogin());

        Instant now = Instant.now();
        long lifeTime = switch (tokenType) {
            case ACCESS -> expirationAccess;
            case REFRESH -> expirationRefresh;
        };

        String idToken = UUID.randomUUID().toString();

        Date createDate = Date.from(now);
        Date expirationDate = Date.from(now.plusSeconds(lifeTime));

        String token = jwtUtils.generateToken(
                Jwts.builder()
                        .id(idToken)
                        .issuer(projectName)
                        .subject(user.getId().toString())
                        .claim("token_type", tokenType.name())
                        .claim("session_id", sessionId)
                        .audience().add("classmate-bot").and()
                        .issuedAt(createDate)
                        .expiration(expirationDate)
                        .claim("service_role", "USER")
        );

        String keyNameRedisJWToken = generateKeyName(tokenType.name(), idToken);

        Tokens tokenModel = new Tokens();
        tokenModel.setToken(token);
        tokenModel.setRevoked(false);
        tokenModel.setCreatedAt(TimeUtils.convertToLocalDateTime(createDate));
        tokenModel.setExpiresAt(TimeUtils.convertToLocalDateTime(expirationDate));
        tokenModel.setOwner(user);
        tokenModel.setTokenType(tokenType.name());
        tokenModel.setSessionId(sessionId);
        user.addToken(tokenModel);

        tokensRepository.save(tokenModel);
        redisService.save(keyNameRedisJWToken, token, lifeTime);

        log.info("Token successfully generated for type [{}]", tokenType);
        return new TokenObject(token, lifeTime);
    }

    public boolean validateToken(String token) {
        return jwtUtils.isTokenValid(token);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void revokeAllUserTokens(Users user) {
        List<Tokens> validTokens = user.getTokens();
        if (validTokens == null || validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            token.setRevoked(true);
            Claims claims = jwtUtils.getClaims(token.getToken());
            if (claims != null) {
                redisService.delete(generateKeyName(token.getTokenType(), claims.getId()));
            } else {
                log.warn("Failed to extract claims for token ID in revokeAllUserTokens()");
            }
        });
        tokensRepository.saveAll(validTokens);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void revokeAllTokensBySessionId(Users user, UUID sessionId) {
        List<Tokens> validTokens = user.getTokens();
        if (validTokens == null || validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            if (sessionId.equals(token.getSessionId())) {
                token.setRevoked(true);
                Claims claims = jwtUtils.getClaims(token.getToken());
                if (claims != null && claims.getId() != null) {
                    String redisKey = generateKeyName(token.getTokenType(), claims.getId());
                    redisService.delete(redisKey);
                    log.info("Revoked token with session_id: {}, jti: {}", sessionId, claims.getId());
                } else {
                    log.warn("Could not revoke token from Redis — claims are null or malformed for session_id: {}", sessionId);
                }
            }
        });
        tokensRepository.saveAll(validTokens);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void refreshToken(String refreshToken, AuthResponse.Builder responseBuilder) {
        Optional<Tokens> tokensOptional = tokensRepository.findByToken(refreshToken);
        if (tokensOptional.isEmpty()) {
            formulateAResponse(HttpResponseStatus.UNAUTHORIZED.code(), "INVALID_CREDENTIALS", "The token is no longer valid", responseBuilder);
            return;
        }

        Tokens refreshTokenForBD = tokensOptional.get();

        if (refreshTokenForBD.getRevoked()) {
            formulateAResponse(
                    HttpResponseStatus.UNAUTHORIZED.code(),
                    "INVALID_CREDENTIALS",
                    "The token is no longer valid",
                    responseBuilder
            );
            return;
        }

        Users owner = refreshTokenForBD.getOwner();
        revokeAllTokensBySessionId(owner, refreshTokenForBD.getSessionId());

        UUID sessionId = UUID.randomUUID();
        TokenObject accessJWT = generateToken(owner, JWTokenType.ACCESS, sessionId);
        TokenObject refreshJWT = generateToken(owner, JWTokenType.REFRESH, sessionId);
        responseBuilder.setStatusCode(HttpResponseStatus.OK.code());
        responseBuilder.setMessageCode(HttpResponseStatus.OK.reasonPhrase());
        responseBuilder.setAccessToken(accessJWT.getToken());
        responseBuilder.setRefreshToken(refreshJWT.getToken());
        responseBuilder.setExpiresIn(accessJWT.getLifeTime());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void logout(LogoutRequest request, AuthResponse.Builder responseBuilder) {
        Optional<Tokens> tokensOptional = tokensRepository.findByToken(request.getRefreshToken());
        if (tokensOptional.isEmpty()) {
            formulateAResponse(HttpResponseStatus.UNAUTHORIZED.code(), "INVALID_CREDENTIALS", "The token is no longer valid", responseBuilder);
            return;
        }

        Tokens token = tokensOptional.get();
        Users owner = token.getOwner();
        if (request.getAllSession()) {
            log.info("Revoke All User Tokens");
            revokeAllUserTokens(owner);
        } else {
            log.info("revoke All Tokens BySession Id: {}", token.getSessionId());
            revokeAllTokensBySessionId(owner, token.getSessionId());
        }
        formulateAResponse(HttpResponseStatus.OK.code(), "OK", responseBuilder);
    }

    private String generateKeyName(String typeToken, String idToken) {
        return format("jwt:%s:%s", typeToken, idToken);
    }
}