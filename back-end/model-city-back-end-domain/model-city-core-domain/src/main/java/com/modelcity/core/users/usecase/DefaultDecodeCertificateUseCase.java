package com.modelcity.core.users.usecase;

import com.modelcity.core.users.exception.CertificateExpiredException;
import com.modelcity.core.users.exception.IncompleteCertificateException;
import com.modelcity.core.users.utils.CertificateUtils;
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

/** Default {@link DecodeCertificateUseCase} implementation. Registered as a fallback bean by the domain auto-config. */
@Service
@ModelCityDisabledIfInherited
public class DefaultDecodeCertificateUseCase implements DecodeCertificateUseCase {

    @Override
    public String parse(
            String subjectHeader,
            String issuerHeader,
            String validityHeader,
            String escapedLeafCertificate
    ) {
        X509Certificate certificate = null;
        if (escapedLeafCertificate != null && !escapedLeafCertificate.isBlank()) {
            certificate = CertificateUtils.parseUrlEncodedPemCertificate(escapedLeafCertificate);
        }

        String subject = certificate != null
                ? certificate.getSubjectX500Principal().getName(X500Principal.RFC2253)
                : subjectHeader;
        String issuer = certificate != null
                ? certificate.getIssuerX500Principal().getName(X500Principal.RFC2253)
                : issuerHeader;
        Date notBeforeDate = certificate != null ? certificate.getNotBefore() : null;
        Date notAfterDate = certificate != null ? certificate.getNotAfter() : null;

        if (isMissing(issuer)) {
            throw new IncompleteCertificateException("issuer");
        }
        if (isMissing(subject)) {
            throw new IncompleteCertificateException("subject");
        }

        Date now = new Date();
        if (notBeforeDate != null && notAfterDate != null
                && (now.before(notBeforeDate) || now.after(notAfterDate))) {
            throw new CertificateExpiredException(notBeforeDate, notAfterDate);
        }

        Map<String, String> dn = CertificateUtils.parseDistinguishedName(subject);
        return CertificateUtils.extractSpanishDocumentNumber(subject, dn);
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank() || "N/A".equalsIgnoreCase(value);
    }
}
