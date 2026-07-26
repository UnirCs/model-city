package com.modelcity.core.security;

import com.modelcity.core.config.CertificateVerificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

/**
 * Issues and verifies the short-lived, core-signed token that carries the verified {@code dni_hash}
 * from the mTLS verification step to the (non-mTLS) operation-authorization step.
 * <p>
 * The token is opaque to the client and tamper-proof: {@code base64url(payload).base64url(hmac)}
 * where {@code payload = dniHash|expiresAtEpochMillis}. The HMAC is keyed with a server-side secret,
 * so the client cannot forge the {@code dni_hash} or extend the expiry. Self-contained (no DB / no
 * shared store), which keeps it working identically across the microservice and monolith topologies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateVerificationTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SEPARATOR = ".";

    private final CertificateVerificationProperties properties;

    /** Builds a signed token binding the given {@code dni_hash} with an expiry {@code ttlSeconds} from now. */
    public String issue(String dniHash) {
        long expiresAt = Instant.now().plusSeconds(properties.getTokenTtlSeconds()).toEpochMilli();
        String payload = dniHash + "|" + expiresAt;
        String encodedPayload = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        String signature = base64Url(sign(encodedPayload));
        return encodedPayload + SEPARATOR + signature;
    }

    /**
     * Validates the signature and expiry of the token and returns the embedded {@code dni_hash}.
     *
     * @throws ResponseStatusException 401 if the token is malformed or its signature is invalid,
     *                                 410 if it has expired.
     */
    public String verifyAndExtractDniHash(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing certificate verification token");
        }
        int dot = token.indexOf(SEPARATOR);
        if (dot <= 0 || dot == token.length() - 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed certificate verification token");
        }
        String encodedPayload = token.substring(0, dot);
        String providedSignature = token.substring(dot + 1);

        String expectedSignature = base64Url(sign(encodedPayload));
        if (!constantTimeEquals(expectedSignature, providedSignature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid certificate verification token");
        }

        String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
        int pipe = payload.lastIndexOf('|');
        if (pipe <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed certificate verification token");
        }
        String dniHash = payload.substring(0, pipe);
        long expiresAt;
        try {
            expiresAt = Long.parseLong(payload.substring(pipe + 1));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Malformed certificate verification token");
        }
        if (Instant.now().toEpochMilli() > expiresAt) {
            throw new ResponseStatusException(HttpStatus.GONE, "Certificate verification token has expired");
        }
        return dniHash;
    }

    private byte[] sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    properties.getTokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to sign verification token", e);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
