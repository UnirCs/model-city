package com.modelcity.mobility.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/** Initialises the Stripe SDK with the secret API key once at startup. */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StripeConfig {

    private final StripeProperties stripeProperties;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        log.info("Stripe SDK initialised for mobility (defaultCurrency={})", stripeProperties.getDefaultCurrency());
    }
}
