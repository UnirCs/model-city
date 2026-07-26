package com.modelcity.core.users.exception;

import java.util.Date;

public class CertificateExpiredException extends RuntimeException {

    public CertificateExpiredException(Date notBefore, Date notAfter) {
        super("Certificate is not valid. Valid from " + notBefore + " to " + notAfter);
    }
}

