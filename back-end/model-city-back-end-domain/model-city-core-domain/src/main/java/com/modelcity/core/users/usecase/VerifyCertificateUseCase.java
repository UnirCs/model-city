package com.modelcity.core.users.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Binds the DNI of a freshly verified mTLS certificate to the authenticated citizen and issues a
 * signed verification token.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultVerifyCertificateUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface VerifyCertificateUseCase {

    /**
     * Validates the certificate DNI against the account binding and returns a signed token carrying
     * the verified {@code dni_hash}.
     *
     * @param sub authenticated Auth0 sub (account owner)
     * @param dni raw DNI extracted from the mTLS certificate
     * @return the verification token to relay to the operation-authorization step
     */
    String bindAndIssueToken(String sub, String dni);
}
