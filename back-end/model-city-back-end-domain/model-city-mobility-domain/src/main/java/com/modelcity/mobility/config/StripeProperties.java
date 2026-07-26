package com.modelcity.mobility.config;

import com.modelcity.common.config.stripe.AbstractStripeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Stripe configuration bound from {@code mobility.stripe.*} properties. */
@Component
@ConfigurationProperties(prefix = "mobility.stripe")
public class StripeProperties extends AbstractStripeProperties {
}
