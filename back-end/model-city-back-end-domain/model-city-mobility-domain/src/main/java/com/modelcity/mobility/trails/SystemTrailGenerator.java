package com.modelcity.mobility.trails;

import com.modelcity.common.trails.AbstractSystemTrailGenerator;
import com.modelcity.common.trails.OperationType;
import com.modelcity.mobility.cars.store.model.CarView;
import com.modelcity.mobility.trails.store.MobilitySystemTrailStore;
import com.modelcity.mobility.reservations.repository.model.ReservationStatus;
import com.modelcity.mobility.reservations.store.model.StreetReservationView;
import com.modelcity.mobility.sanctions.store.model.SanctionView;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/** Records mobility system trails (cars, street reservations, sanctions). */
@Service("mobilitySystemTrailGenerator")
public class SystemTrailGenerator extends AbstractSystemTrailGenerator {

    private static final String CITIZEN_ROLE = "MODEL-CITY-CITIZEN";
    private static final String RES_CAR = "CAR";
    private static final String RES_STREET_RESERVATION = "STREET_RESERVATION";
    private static final String RES_SANCTION = "SANCTION";

    public SystemTrailGenerator(MobilitySystemTrailStore store, ObjectMapper objectMapper) {
        super(store, objectMapper);
    }

    public void carRegistered(CarView car) {
        record("CAR_REGISTERED", OperationType.CREATE, car.getOwnerSub(), CITIZEN_ROLE,
                null, null, RES_CAR, String.valueOf(car.getId()),
                payload("carId", car.getId(), "ownerSub", car.getOwnerSub(),
                        "licensePlate", car.getLicensePlate(), "nickname", car.getNickname(),
                        "brand", car.getBrand(), "model", car.getModel()));
    }

    public void streetReservationCreated(StreetReservationView r) {
        record("STREET_RESERVATION_CREATED", OperationType.CREATE, r.getUserSub(), CITIZEN_ROLE,
                null, null, RES_STREET_RESERVATION, String.valueOf(r.getId()), reservationPayload(r));
    }

    public void streetReservationRenewed(StreetReservationView r) {
        record("STREET_RESERVATION_RENEWED", OperationType.CREATE, r.getUserSub(), CITIZEN_ROLE,
                null, null, RES_STREET_RESERVATION, String.valueOf(r.getId()), reservationPayload(r));
    }

    /** Stripe webhook outcome — actor is the citizen who owns the reservation. */
    public void streetReservationStatusChanged(String userSub, String checkoutSessionId, ReservationStatus newStatus, String stripeEventType) {
        String eventType = newStatus == ReservationStatus.PAID
                ? "STREET_RESERVATION_CONFIRMED" : "STREET_RESERVATION_CANCELLED";
        record(eventType, OperationType.UPDATE, userSub, CITIZEN_ROLE,
                null, null, RES_STREET_RESERVATION, checkoutSessionId,
                payload("checkoutSessionId", checkoutSessionId, "previousStatus", "PENDING",
                        "newStatus", newStatus.name(), "stripeEventType", stripeEventType));
    }

    public void sanctionIssued(SanctionView s) {
        record("SANCTION_ISSUED", OperationType.CREATE, s.getAgentSub(), null,
                null, null, RES_SANCTION, String.valueOf(s.getId()),
                payload("sanctionId", s.getId(), "licensePlate", s.getLicensePlate(),
                        "latitude", s.getLatitude(), "longitude", s.getLongitude(),
                        "agentSub", s.getAgentSub(),
                        "createdAt", s.getCreatedAt() == null ? null : s.getCreatedAt().toString()));
    }

    private static Object reservationPayload(StreetReservationView r) {
        CarView car = r.getCar();
        return payload(
                "reservationId", r.getId(),
                "userSub", r.getUserSub(),
                "carId", car == null ? null : car.getId(),
                "licensePlate", car == null ? null : car.getLicensePlate(),
                "latitude", r.getLatitude(),
                "longitude", r.getLongitude(),
                "createdAt", r.getCreatedAt() == null ? null : r.getCreatedAt().toString(),
                "expiresAt", r.getExpiresAt() == null ? null : r.getExpiresAt().toString(),
                "renewedFromId", r.getRenewedFromId(),
                "pricePaid", r.getPricePaid(),
                "currency", r.getCurrency());
    }
}
