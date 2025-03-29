package com.secureuser.service.service;

import com.secureuser.service.constants.JWTokenType;
import com.secureuser.service.dto.TokenObject;
import com.secureuser.service.model.Users;
import com.secureuser.service.proto.user.auth.AuthResponse;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.secureuser.service.utils.GRPCHelperMessage.formulateAResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    @Value("${spring.user.registration.require-verification}")
    private boolean isRequireVerification;
    private final UsersService usersService;
    private final BCryptPasswordEncoder encoder;
    private final TokenService tokenService;

    public void authenticationWithEmail(String loginOrEmail, String password, UUID sessionId, AuthResponse.Builder responseBuilder) {
        Optional<Users> usersOptional = findUserByLoginOrEmail(loginOrEmail, responseBuilder);
        if (usersOptional.isEmpty()) {
            return;
        }

        Users user = usersOptional.get();
        if (!checkAccountConfirmation(user, responseBuilder)) {
            return;
        }
        log.info("Check password");
        if (encoder.matches(password, user.getPassword())) {
            log.info("Password matches");
            TokenObject accessJWT = tokenService.generateToken(user, JWTokenType.ACCESS, sessionId);
            TokenObject refreshJWT = tokenService.generateToken(user, JWTokenType.REFRESH, sessionId);
            responseBuilder.setStatusCode(HttpResponseStatus.OK.code());
            responseBuilder.setMessageCode(HttpResponseStatus.OK.reasonPhrase());
            responseBuilder.setAccessToken(accessJWT.getToken());
            responseBuilder.setRefreshToken(refreshJWT.getToken());
            responseBuilder.setExpiresIn(accessJWT.getLifeTime());
            responseBuilder.setSessionId(sessionId.toString());
        } else {
            log.info("Password does not match");
            formulateAResponse(HttpResponseStatus.UNAUTHORIZED.code(), "INVALID_CREDENTIALS", "Invalid credentials", responseBuilder);
        }
    }

    private Optional<Users> findUserByLoginOrEmail(String loginOrEmail, AuthResponse.Builder responseBuilder) {
        log.info("Find user by identificator: {}", loginOrEmail);
        Optional<Users> result = usersService.findByLoginOrEmail(loginOrEmail, loginOrEmail);
        if (result.isEmpty()) {
            log.info("User '{}' not found ", loginOrEmail);
            formulateAResponse(HttpResponseStatus.UNAUTHORIZED.code(), "INVALID_CREDENTIALS", "Invalid credentials", responseBuilder);
        }
        return result;
    }

    private Boolean checkAccountConfirmation(Users user, AuthResponse.Builder responseBuilder) {
        log.info("Verification of a confirmed account");
        log.info("Verification enabled or disabled: {}", isRequireVerification);
        if (!isRequireVerification) {
            log.info("Verification disabled");
            return true;
        }

        if (!user.getIsVerified()) {
            formulateAResponse(HttpResponseStatus.FORBIDDEN.code(), "ACCOUNT_NOT_VERIFIED", "ACCOUNT_NOT_VERIFIED", responseBuilder);
            return false;
        }
        return true;
    }
}