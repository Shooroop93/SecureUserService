package com.secureuser.service.service;

import com.secureuser.service.model.Users;
import com.secureuser.service.proto.user.auth.AuthResponse;
import com.secureuser.service.proto.user.auth.Error;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    @Value("${spring.user.registration.require-verification}")
    private boolean isRequireVerification;
    private final UsersService usersService;

    public void authenticationWithEmail(String loginOrEmail, String password, AuthResponse.Builder responseBuilder) {
        Optional<Users> user = findUserByLoginOrEmail(loginOrEmail, responseBuilder);
        if (user.isEmpty()) {
            return;
        }

        if (!checkAccountConfirmation(user.get(), responseBuilder)) {
            return;
        }
    }

    private Optional<Users> findUserByLoginOrEmail(String loginOrEmail, AuthResponse.Builder responseBuilder) {
        log.info("Find user by identificator: {}", loginOrEmail);
        Optional<Users> result = usersService.findByLoginOrEmail(loginOrEmail, loginOrEmail);
        if (result.isEmpty()) {
            log.info("User '{}' not fount ", loginOrEmail);
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

    private void formulateAResponse(int statusCode, String messageCode, String errorMessage, AuthResponse.Builder responseBuilder) {
        log.info("Formulate error response: [{} - {}] {}", statusCode, messageCode, errorMessage);
        responseBuilder.setStatusCode(statusCode)
                .setMessageCode(messageCode)
                .setError(Error.newBuilder().setErrorMessage(errorMessage));
    }
}