package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;

/**
 * Parses and validates an mTLS client certificate, extracting the raw DNI.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDecodeCertificateUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DecodeCertificateUseCase {

    String parse(String subjectHeader, String issuerHeader, String validityHeader, String escapedLeafCertificate);
}
