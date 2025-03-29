package com.secureuser.service.utils;

import com.secureuser.service.proto.user.auth.AuthResponse;
import com.secureuser.service.proto.user.auth.Error;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRPCHelperMessage {

    public static void formulateAResponse(int statusCode, String messageCode, AuthResponse.Builder responseBuilder) {
        formulateAResponse(statusCode, messageCode, Error.newBuilder().build(), responseBuilder);
    }

    public static void formulateAResponse(int statusCode, String messageCode, String errorMessage, AuthResponse.Builder responseBuilder) {
        formulateAResponse(statusCode, messageCode, Error.newBuilder().setErrorMessage(errorMessage).build(), responseBuilder);

    }

    private static void formulateAResponse(int statusCode, String messageCode, Error errorMessage, AuthResponse.Builder responseBuilder) {
        log.info("Formulate error response: [{} - {}] {}", statusCode, messageCode, errorMessage);
        responseBuilder.setStatusCode(statusCode)
                .setMessageCode(messageCode)
                .setError(errorMessage);
    }
}