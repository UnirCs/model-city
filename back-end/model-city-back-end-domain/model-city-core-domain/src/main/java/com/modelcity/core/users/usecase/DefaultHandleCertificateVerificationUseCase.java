package com.modelcity.core.users.usecase;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import com.modelcity.core.security.DniHasher;
import com.modelcity.core.users.controller.model.CertificateIdentityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Default {@link HandleCertificateVerificationUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@RequiredArgsConstructor
@Service
@ModelCityDisabledIfInherited
public class DefaultHandleCertificateVerificationUseCase implements HandleCertificateVerificationUseCase {

    private final DecodeCertificateUseCase decodeCertificateUseCase;
    private final VerifyCertificateUseCase verifyCertificateUseCase;
    private final DniHasher dniHasher;

    @Override
    public CertificateIdentityDto execute(String sub, String subject, String issuer, String validity, String leaf) {
        if (sub == null || sub.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authentication is required to verify a certificate");
        }
        String rawDni = decodeCertificateUseCase.parse(valueOrNA(subject), valueOrNA(issuer), valueOrNA(validity), leaf);
        String token = verifyCertificateUseCase.bindAndIssueToken(sub, rawDni);
        return new CertificateIdentityDto(dniHasher.hash(rawDni), token);
    }

    private String valueOrNA(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
