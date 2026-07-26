package com.modelcity.core.security;

import com.modelcity.core.config.CertificateVerificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Derives an irreversible, fixed-size token from a raw Spanish DNI.
 * <p>
 * The DNI space is tiny (~10^8), so a plain hash would be trivially brute-forceable; we use an
 * HMAC keyed with a server-side pepper so the output cannot be reversed or pre-computed without
 * the secret. The raw DNI never leaves core — only this hash is persisted and propagated.
 */
@Component
@RequiredArgsConstructor
public class DniHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final CertificateVerificationProperties properties;

    /** Returns the lowercase hex HMAC-SHA256 of the normalised DNI. 64 chars. */
    public String hash(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Cannot hash a blank DNI");
        }
        String normalised = dni.trim().toUpperCase();
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    properties.getDniPepper().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(normalised.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to compute DNI hash", e);
        }
    }
}
