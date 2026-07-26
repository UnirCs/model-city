package com.modelcity.leisure.events.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;import com.modelcity.leisure.events.store.model.EventTicketView;

import com.modelcity.leisure.config.StripeProperties;
import com.modelcity.leisure.events.store.EventTicketStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;

/**
 * Default concrete {@link StripeWebhookController}. The component-scanned platform default; disabled at
 * startup when a local deployment provides its own bean for the seam.
 */
@RestController("leisureDefaultStripeWebhookController")
@ModelCityDisabledIfInherited
public class DefaultStripeWebhookController extends StripeWebhookController {

    public DefaultStripeWebhookController(
            StripeProperties stripeProperties,
            EventTicketStore<? extends EventTicketView> eventTicketStore,
            SystemTrailGenerator systemEventGenerator) {
        super(stripeProperties, eventTicketStore, systemEventGenerator);
    }
}
