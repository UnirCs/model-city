package com.modelcity.core.users.exception;

public class IncompleteCertificateException extends RuntimeException {

    public IncompleteCertificateException(String field) {
        super("Certificate is missing required field: " + field);
    }
}

