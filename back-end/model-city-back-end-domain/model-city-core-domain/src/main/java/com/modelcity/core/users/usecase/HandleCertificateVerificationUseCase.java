package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.core.users.controller.model.CertificateIdentityDto;

/**
 * Orchestrates the full certificate-verification endpoint: decodes the mTLS headers,
 * binds the DNI to the authenticated account, and returns the identity with a verification token.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultHandleCertificateVerificationUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface HandleCertificateVerificationUseCase {

    CertificateIdentityDto execute(String sub, String subject, String issuer, String validity, String leaf);
}
