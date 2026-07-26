package com.modelcity.core.users.exception;

public class CertificateNotVerifiedException extends RuntimeException {

    public CertificateNotVerifiedException(String verifyHeader) {
        super("Certificate verification failed. Header value: " + verifyHeader);
    }
}

