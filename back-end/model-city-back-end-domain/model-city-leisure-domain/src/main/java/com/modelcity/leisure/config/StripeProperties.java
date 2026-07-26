package com.modelcity.leisure.config;

import com.modelcity.common.config.stripe.AbstractStripeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Stripe configuration bound from {@code leisure.stripe.*} properties. */
@Component("leisureStripeProperties")
@ConfigurationProperties(prefix = "leisure.stripe")
public class StripeProperties extends AbstractStripeProperties {
}
