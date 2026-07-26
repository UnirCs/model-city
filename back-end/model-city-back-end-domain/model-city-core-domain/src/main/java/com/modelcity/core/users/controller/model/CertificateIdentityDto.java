package com.modelcity.core.users.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CertificateIdentityDto {

    /** HMAC-SHA256 of the citizen's DNI. */
    private String dni;

    /**
     * Short-lived, core-signed token proving this DNI was just verified via mTLS. The front-end
     * relays it to {@code POST /operation-authorizations} so the verified {@code dni_hash} can be
     * bound to the challenge without the client ever handling (or being able to forge) the raw DNI.
     */
    private String verificationToken;
}
