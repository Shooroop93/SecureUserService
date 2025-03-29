package com.secureuser.service.grpc;

import com.secureuser.service.exception.DatabaseOperationException;
import com.secureuser.service.proto.user.auth.AuthResponse;
import com.secureuser.service.proto.user.auth.AuthServiceGrpc;
import com.secureuser.service.proto.user.auth.LoginRequest;
import com.secureuser.service.proto.user.auth.RefreshTokenRequest;
import com.secureuser.service.proto.user.auth.RegisterRequest;
import com.secureuser.service.service.LoginService;
import com.secureuser.service.service.RegistrationConfirmationService;
import com.secureuser.service.service.TokenService;
import com.secureuser.service.service.UsersService;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static com.secureuser.service.utils.GRPCHelperMessage.formulateAResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final UsersService usersService;
    private final RegistrationConfirmationService registrationConfirmationService;
    private final LoginService loginService;
    private final TokenService tokenService;

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> response) {
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();
        if (request.getLogin().isBlank() || request.getPassword().isBlank() || !EmailValidator.getInstance().isValid(request.getEmail())) {
            formulateAResponse(
                    HttpResponseStatus.BAD_REQUEST.code(),
                    "BAD_REQUEST",
                    "Incorrectly filled data in the request",
                    responseBuilder
            );

            sendErrorMessage(responseBuilder, response);
            return;
        }

        if (usersService.existsByLoginOrEmail(request.getLogin(), request.getEmail())) {
            formulateAResponse(
                    HttpResponseStatus.CONFLICT.code(),
                    "USER_ALREADY_EXISTS",
                    "Incorrectly filled data in the request",
                    responseBuilder
            );

            sendErrorMessage(responseBuilder, response);
            return;
        }

        try {
            usersService.save(request.getLogin(), request.getEmail(), request.getPassword());

            formulateAResponse(
                    HttpResponseStatus.CREATED.code(),
                    "CREATED",
                    responseBuilder
            );

            Map<String, String> save = registrationConfirmationService.save(request.getLogin());
            if (save != null) {
                responseBuilder.setRegistrationConfirmationLink(save.get("url"));
            }
            response.onNext(responseBuilder.build());
            response.onCompleted();

            log.info("User registered successfully: {}", request.getLogin());
        } catch (DatabaseOperationException e) {
            formulateAResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                    "INTERNAL_SERVER_ERROR",
                    e.getMessage(),
                    responseBuilder
            );

            sendErrorMessage(responseBuilder, response);
        }
    }

    @Override
    public void login(LoginRequest request, StreamObserver<AuthResponse> response) {
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();
        if (request.getLogin().isBlank() || request.getPassword().isBlank()) {

            formulateAResponse(
                    HttpResponseStatus.BAD_REQUEST.code(),
                    "BAD_REQUEST",
                    "Incorrectly filled data in the request",
                    responseBuilder
            );

            sendErrorMessage(responseBuilder, response);
            return;
        }

        log.info("Attempting login: {}", request.getLogin());
        UUID sessionId = UUID.randomUUID();
        loginService.authenticationWithEmail(request.getLogin(), request.getPassword(), sessionId, responseBuilder);

        response.onNext(responseBuilder.build());
        response.onCompleted();
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<AuthResponse> response) {
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();
        if (request.getRefreshToken().isBlank()) {

            formulateAResponse(
                    HttpResponseStatus.BAD_REQUEST.code(),
                    "BAD_REQUEST",
                    "Incorrectly filled data in the request",
                    responseBuilder
            );
            sendErrorMessage(responseBuilder, response);
            return;
        }

        log.info("Refresh token start");
        tokenService.refreshToken(request.getRefreshToken(), responseBuilder);
        log.info("Refresh token end");

        response.onNext(responseBuilder.build());
        response.onCompleted();
    }

    private void sendErrorMessage(AuthResponse.Builder responseBuilder, StreamObserver<AuthResponse> response) {
        AuthResponse result = responseBuilder.build();
        log.info("Sending error response: [{} - {}] {}", result.getStatusCode(), result.getMessageCode(), result.getError().getErrorMessage());
        response.onNext(result);
        response.onCompleted();
    }
}