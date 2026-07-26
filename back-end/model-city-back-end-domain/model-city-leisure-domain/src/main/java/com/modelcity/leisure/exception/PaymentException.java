package com.modelcity.leisure.exception;

import com.modelcity.common.exception.ModelCityException;

/** Raised when an upstream payment provider call fails. Mapped to HTTP 502. */
public class PaymentException extends ModelCityException {

    public PaymentException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
