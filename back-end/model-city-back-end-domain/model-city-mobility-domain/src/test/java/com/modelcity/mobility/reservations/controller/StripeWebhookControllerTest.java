package com.modelcity.mobility.reservations.controller;

import com.modelcity.mobility.config.StripeProperties;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import com.modelcity.mobility.reservations.store.StreetReservationStore;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import com.modelcity.mobility.trails.SystemTrailGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StripeWebhookControllerTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    @Mock
    @SuppressWarnings("unchecked")
    StreetReservationStore<StreetReservationView> streetReservationStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    DefaultStripeWebhookController controller;

    @BeforeEach
    void setUp() {
        StripeProperties properties = new StripeProperties();
        properties.setWebhookSecret(WEBHOOK_SECRET);
        controller = new DefaultStripeWebhookController(properties, streetReservationStore, systemTrailGenerator);
    }

    private String signedHeader(String payload) {
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;
        String signature = hmacSha256Hex(signedPayload, WEBHOOK_SECRET);
        return "t=" + timestamp + ",v1=" + signature;
    }

    private String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String eventPayload(String type, String checkoutSessionId) {
        return """
                {
                  "id": "evt_test_1",
                  "object": "event",
                  "api_version": "%s",
                  "type": "%s",
                  "data": {
                    "object": {
                      "id": "%s",
                      "object": "checkout.session"
                    }
                  }
                }
                """.formatted(com.stripe.Stripe.API_VERSION, type, checkoutSessionId);
    }

    @Test
    void handleWebhook_invalidSignature_returnsBadRequest() {
        String payload = eventPayload("checkout.session.completed", "cs_test_123");

        ResponseEntity<?> response = controller.handleWebhook(payload.getBytes(StandardCharsets.UTF_8), "invalid-signature");

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(streetReservationStore);
    }

    @Test
    void handleWebhook_checkoutSessionCompleted_marksReservationPaidAndAudits() {
        StreetReservationView reservation = mock(StreetReservationView.class);
        when(reservation.getUserSub()).thenReturn("user-sub");
        when(streetReservationStore.markStatusByCheckoutSession("cs_test_123", ReservationStatus.PAID))
                .thenReturn(Optional.of(reservation));

        String payload = eventPayload("checkout.session.completed", "cs_test_123");
        String signature = signedHeader(payload);

        ResponseEntity<?> response = controller.handleWebhook(payload.getBytes(StandardCharsets.UTF_8), signature);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(streetReservationStore).markStatusByCheckoutSession("cs_test_123", ReservationStatus.PAID);
        verify(systemTrailGenerator).streetReservationStatusChanged(
                eq("user-sub"), eq("cs_test_123"), eq(ReservationStatus.PAID), any());
    }

    @Test
    void handleWebhook_checkoutSessionExpired_marksReservationCancelled() {
        StreetReservationView reservation = mock(StreetReservationView.class);
        when(reservation.getUserSub()).thenReturn("user-sub");
        when(streetReservationStore.markStatusByCheckoutSession("cs_test_123", ReservationStatus.CANCELLED))
                .thenReturn(Optional.of(reservation));

        String payload = eventPayload("checkout.session.expired", "cs_test_123");
        String signature = signedHeader(payload);

        controller.handleWebhook(payload.getBytes(StandardCharsets.UTF_8), signature);

        verify(streetReservationStore).markStatusByCheckoutSession("cs_test_123", ReservationStatus.CANCELLED);
    }

    @Test
    void handleWebhook_unknownEventType_isIgnored() {
        String payload = eventPayload("payment_intent.created", "cs_test_123");
        String signature = signedHeader(payload);

        ResponseEntity<?> response = controller.handleWebhook(payload.getBytes(StandardCharsets.UTF_8), signature);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verifyNoInteractions(streetReservationStore);
    }

    @Test
    void handleWebhook_noMatchingReservation_doesNotThrow() {
        when(streetReservationStore.markStatusByCheckoutSession("cs_unknown", ReservationStatus.PAID))
                .thenReturn(Optional.empty());

        String payload = eventPayload("checkout.session.completed", "cs_unknown");
        String signature = signedHeader(payload);

        ResponseEntity<?> response = controller.handleWebhook(payload.getBytes(StandardCharsets.UTF_8), signature);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verifyNoInteractions(systemTrailGenerator);
    }
}
