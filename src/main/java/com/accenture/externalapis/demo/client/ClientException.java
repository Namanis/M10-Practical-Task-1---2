package com.accenture.externalapis.demo.client;

public class ClientException extends RuntimeException {

    private final Integer statusCode;

    public ClientException(String message) {
        super(message);
        this.statusCode = null;
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }

    public ClientException(String message, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}
