package com.modelcity.leisure.events.controller;
import com.modelcity.leisure.events.store.model.EventTicketView;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
import com.modelcity.leisure.config.StripeProperties;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import com.modelcity.leisure.events.store.EventTicketStore;
import com.modelcity.leisure.trails.SystemTrailGenerator;
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

/** Handles Stripe webhook callbacks for event ticket payment lifecycle events (web Checkout Session flow). */
@Slf4j
@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class StripeWebhookController {

    protected final StripeProperties stripeProperties;
    protected final EventTicketStore<? extends EventTicketView> eventTicketStore;
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
            case "checkout.session.completed" -> updateByCheckoutSession(event, TicketStatus.PAID);
            case "checkout.session.expired",
                 "checkout.session.async_payment_failed" -> updateByCheckoutSession(event, TicketStatus.CANCELLED);
            default -> log.debug("Ignoring Stripe event type={}", event.getType());
        }

        return ResponseEntity.ok(Map.of("received", true));
    }

    private void updateByCheckoutSession(Event event, TicketStatus newStatus) {
        event.getDataObjectDeserializer().getObject()
                .filter(Session.class::isInstance)
                .map(Session.class::cast)
                .ifPresentOrElse(session -> {
                    String csId = session.getId();
                    eventTicketStore.findByCheckoutSessionId(csId).ifPresentOrElse(ticket -> {
                        eventTicketStore.markStatus(ticket.getId(), newStatus);
                        systemEventGenerator.eventTicketStatusChanged(ticket, newStatus, event.getType());
                        log.info("Ticket id={} updated to status={} (cs={})", ticket.getId(), newStatus, csId);
                    }, () -> log.warn("No ticket found for checkout session id={}", csId));
                }, () -> log.warn("Stripe event {} did not contain a Session payload", event.getId()));
    }
}
