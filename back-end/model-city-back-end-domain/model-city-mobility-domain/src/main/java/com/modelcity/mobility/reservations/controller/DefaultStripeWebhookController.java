package com.modelcity.mobility.reservations.controller;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

import com.modelcity.mobility.config.StripeProperties;
import com.modelcity.mobility.trails.SystemTrailGenerator;
import com.modelcity.mobility.reservations.store.StreetReservationStore;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;

/**
 * Default concrete {@link StripeWebhookController}. The component-scanned platform default; disabled at startup when a local
 * deployment provides its own bean for the seam.
 */
@RestController("mobilityDefaultStripeWebhookController")
@ModelCityDisabledIfInherited
public class DefaultStripeWebhookController extends StripeWebhookController {

    public DefaultStripeWebhookController(
            StripeProperties stripeProperties,
            StreetReservationStore<? extends StreetReservationView> streetReservationStore,
            SystemTrailGenerator systemEventGenerator) {
        super(stripeProperties, streetReservationStore, systemEventGenerator);
    }
}
