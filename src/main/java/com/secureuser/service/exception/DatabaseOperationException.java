package com.secureuser.service.exception;

public class DatabaseOperationException extends RuntimeException {

    public DatabaseOperationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(Throwable throwable) {
        super(throwable);
    }
}