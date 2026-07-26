package com.modelcity.common.config.stripe;

import lombok.Getter;
import lombok.Setter;

/** Common Stripe configuration fields bound from a module-specific {@code *.stripe.*} prefix. */
@Getter
@Setter
public abstract class AbstractStripeProperties {

    /** Stripe secret API key (sk_…). */
    private String secretKey;

    /** Stripe webhook signing secret (whsec_…). */
    private String webhookSecret;

    /** Default ISO currency used when a resource does not specify one. */
    private String defaultCurrency = "eur";
}
