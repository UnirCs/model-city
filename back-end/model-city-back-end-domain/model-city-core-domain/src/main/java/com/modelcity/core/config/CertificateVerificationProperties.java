package com.modelcity.core.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Secrets and tuning for mTLS certificate verification.
 * <p>
 * {@code dniPepper} is the HMAC key used to derive the irreversible {@code dni_hash} from the raw
 * DNI extracted from the certificate — it never leaves core, so downstream services only ever see
 * the opaque hash. {@code tokenSecret} signs the short-lived verification token handed to the
 * front-end so the DNI proof can be relayed to a later, non-mTLS call without being forgeable.
 * <p>
 * Both secrets are validated at startup: {@code @ConfigurationProperties} silently leaves an
 * unresolved placeholder (e.g. {@code "${CERT_DNI_PEPPER}"}) as its literal value instead of failing,
 * which would let the app boot with a public, guessable HMAC key and break the irreversibility
 * guarantee. The {@code @Pattern} rejects that literal so a missing env var fails fast.
 */
@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "certificate.verification")
public class CertificateVerificationProperties {

    /** Rejects an unresolved {@code ${...}} placeholder left in place by a missing env var. */
    private static final String NOT_A_PLACEHOLDER = "^(?!\\$\\{).+";

    /** HMAC-SHA256 key used to derive {@code dni_hash} from the raw DNI. Must be kept secret. */
    @NotBlank
    @Pattern(regexp = NOT_A_PLACEHOLDER, message = "CERT_DNI_PEPPER is not set (unresolved placeholder)")
    private String dniPepper;

    /** HMAC-SHA256 key used to sign the certificate verification token. Must be kept secret. */
    @NotBlank
    @Pattern(regexp = NOT_A_PLACEHOLDER, message = "CERT_TOKEN_SECRET is not set (unresolved placeholder)")
    private String tokenSecret;

    /** Lifetime of the verification token, in seconds. */
    @Positive
    private long tokenTtlSeconds = 300;
}
