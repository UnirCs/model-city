package com.modelcity.core.users.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.users.controller.model.CertificateIdentityDto;
import com.modelcity.core.users.usecase.HandleCertificateVerificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * mTLS certificate-verification endpoint.
 *
 * <p>Modelled as the creation of a <em>certificate verification</em> resource: the use case binds the
 * hashed DNI to the authenticated account and issues a verification token, so it is a {@code POST} to the
 * {@code /certificate-verifications} collection rather than a {@code GET}.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultCertificateVerificationsController}
 * as the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends CertificateVerificationsController}; the default then backs off.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/certificate-verifications")
@ModelCityExtensionPoint
public abstract class CertificateVerificationsController {

    protected final HandleCertificateVerificationUseCase certificateVerificationUseCase;

    /**
     * Verifies an mTLS client certificate and returns {@code dni} (hash) and {@code verificationToken}.
     * The certificate travels in the {@code X-Amzn-Mtls-Clientcert-*} headers added by the ALB.
     */
    @PostMapping
    public ResponseEntity<CertificateIdentityDto> verify(
            @RequestHeader(value = "X-Auth-Sub", required = false) String sub,
            @RequestHeader(value = "X-Amzn-Mtls-Clientcert-Subject", required = false) String subject,
            @RequestHeader(value = "X-Amzn-Mtls-Clientcert-Issuer", required = false) String issuer,
            @RequestHeader(value = "X-Amzn-Mtls-Clientcert-Validity", required = false) String validity,
            @RequestHeader(value = "X-Amzn-Mtls-Clientcert-Leaf", required = false) String leaf) {
        return ResponseEntity.ok(certificateVerificationUseCase.execute(sub, subject, issuer, validity, leaf));
    }
}
