package com.modelcity.mobility.reservations.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.mobility.config.StripeProperties;
import com.modelcity.mobility.trails.SystemTrailGenerator;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import com.modelcity.mobility.reservations.store.StreetReservationStore;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles Stripe webhook callbacks for street reservation payment events.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultStripeWebhookController} as
 * the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends StripeWebhookController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class StripeWebhookController {

    protected final StripeProperties stripeProperties;
    protected final StreetReservationStore<? extends StreetReservationView> streetReservationStore;
    protected final SystemTrailGenerator systemEventGenerator;

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleWebhook(
            @RequestBody byte[] payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(
                    new String(payload, StandardCharsets.UTF_8),
                    sigHeader,
                    stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            log.warn("Invalid Stripe webhook signature: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_signature"));
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> updateStatus(event, ReservationStatus.PAID);
            case "checkout.session.expired",
                 "checkout.session.async_payment_failed" -> updateStatus(event, ReservationStatus.CANCELLED);
            default -> log.debug("Ignoring Stripe event type={}", event.getType());
        }

        return ResponseEntity.ok(Map.of("received", true));
    }

    private void updateStatus(Event event, ReservationStatus newStatus) {
        event.getDataObjectDeserializer().getObject()
                .filter(Session.class::isInstance)
                .map(Session.class::cast)
                .ifPresentOrElse(
                        session -> streetReservationStore.markStatusByCheckoutSession(session.getId(), newStatus)
                                .ifPresent(r -> systemEventGenerator.streetReservationStatusChanged(
                                        r.getUserSub(), session.getId(), newStatus, event.getType())),
                        () -> log.warn("Stripe event {} did not contain a Session payload", event.getId()));
    }
}
