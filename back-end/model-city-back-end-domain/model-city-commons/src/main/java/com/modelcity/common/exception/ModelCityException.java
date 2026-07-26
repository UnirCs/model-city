package com.modelcity.common.exception;

public class ModelCityException extends RuntimeException {

    private final String errorCode;

    public ModelCityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ModelCityException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

