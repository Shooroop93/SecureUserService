package com.secureuser.service.service;

import com.secureuser.service.constants.JWTokenType;
import com.secureuser.service.dto.TokenObject;
import com.secureuser.service.model.Tokens;
import com.secureuser.service.model.Users;
import com.secureuser.service.repository.TokensRepository;
import com.secureuser.service.utils.JwtUtils;
import com.secureuser.service.utils.TimeUtils;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    public TokenObject generateToken(Users user, JWTokenType tokenType) {
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
        user.addToken(tokenModel);

        tokensRepository.save(tokenModel);
        redisService.save(keyNameRedisJWToken, token, lifeTime);

        log.info("Token successfully generated for type [{}]", tokenType);
        return new TokenObject(token, lifeTime);
    }

    public boolean validateToken(String token) {
        return jwtUtils.isTokenValid(token);
    }

    public void revokeAllUserTokens(Users user) {
        List<Tokens> validTokens = user.getTokens();
        validTokens.forEach(token -> {
            token.setRevoked(true);
            redisService.delete(generateKeyName(token.getTokenType(), token.getToken()));
        });
        tokensRepository.saveAll(validTokens);
    }

    private String generateKeyName(String typeToken, String idToken) {
        return format("jwt:%s:%s", typeToken, idToken);
    }
}