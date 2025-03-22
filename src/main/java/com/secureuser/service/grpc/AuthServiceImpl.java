package com.secureuser.service.grpc;

import com.secureuser.service.exception.DatabaseOperationException;
import com.secureuser.service.proto.user.auth.AuthResponse;
import com.secureuser.service.proto.user.auth.AuthServiceGrpc;
import com.secureuser.service.proto.user.auth.Error;
import com.secureuser.service.proto.user.auth.RegisterRequest;
import com.secureuser.service.service.RegistrationConfirmationService;
import com.secureuser.service.service.UsersService;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final UsersService usersService;
    private final RegistrationConfirmationService registrationConfirmationService;

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> response) {
        if (request.getLogin().isBlank() || request.getPassword().isBlank()) {
            sendErrorMessage(HttpResponseStatus.BAD_REQUEST.code(), "BAD_REQUEST", "Incorrectly filled data in the request", response);
            return;
        }

        if (usersService.existsByLoginOrEmail(request.getLogin(), request.getEmail())) {
            sendErrorMessage(HttpResponseStatus.CONFLICT.code(), "USER_ALREADY_EXISTS", "User already exists", response);
            return;
        }

        try {
            usersService.save(request.getLogin(), request.getEmail(), request.getPassword());
            AuthResponse.Builder responseBuilder = AuthResponse.newBuilder()
                    .setStatusCode(HttpResponseStatus.CREATED.code())
                    .setMessageCode("CREATED");

            Map<String, String> save = registrationConfirmationService.save(request.getLogin());
            if (save != null) {
                responseBuilder.setRegistrationConfirmationLink(save.get("url"));
            }
            response.onNext(responseBuilder.build());
            response.onCompleted();

            log.info("User registered successfully: {}", request.getLogin());
        } catch (DatabaseOperationException e) {
            sendErrorMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "INTERNAL_SERVER_ERROR", e.getMessage(), response);
        }
    }

    private void sendErrorMessage(int statusCode, String messageCode, String errorMessage, StreamObserver<AuthResponse> response) {
        log.info("Sending error response: [{} - {}] {}", statusCode, messageCode, errorMessage);
        response.onNext(AuthResponse.newBuilder()
                .setStatusCode(statusCode)
                .setMessageCode(messageCode)
                .setError(Error.newBuilder().setErrorMessage(errorMessage).build())
                .build());
        response.onCompleted();
    }
}