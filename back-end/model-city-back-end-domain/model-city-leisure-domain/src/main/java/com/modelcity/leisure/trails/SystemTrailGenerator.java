package com.modelcity.leisure.trails;

import com.modelcity.common.trails.AbstractSystemTrailGenerator;
import com.modelcity.common.trails.OperationType;
import com.modelcity.leisure.cityplaces.store.model.CityPlaceView;
import com.modelcity.leisure.cityroutes.store.model.CityRouteView;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.publicspaces.model.PublicSpaceView;
import com.modelcity.leisure.publicspaces.model.ReservableResourceView;
import com.modelcity.leisure.publicspaces.model.SpaceReservationView;
import com.modelcity.leisure.trails.store.LeisureSystemTrailStore;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

/** Records leisure system trails (city places/routes, public spaces, reservations, events, tickets, refunds). */
@Service("leisureSystemTrailGenerator")
public class SystemTrailGenerator extends AbstractSystemTrailGenerator {

    private static final String CITIZEN_ROLE = "MODEL-CITY-CITIZEN";
    private static final String RES_CITY_PLACE = "CITY_PLACE";
    private static final String RES_CITY_ROUTE = "CITY_ROUTE";
    private static final String RES_PUBLIC_SPACE = "PUBLIC_SPACE";
    private static final String RES_RESERVABLE_RESOURCE = "RESERVABLE_RESOURCE";
    private static final String RES_SPACE_RESERVATION = "SPACE_RESERVATION";
    private static final String RES_EVENT = "EVENT";
    private static final String RES_EVENT_TICKET = "EVENT_TICKET";

    public SystemTrailGenerator(LeisureSystemTrailStore store, ObjectMapper objectMapper) {
        super(store, objectMapper);
    }

    public void cityPlaceCreated(String sub, CityPlaceView p) { cityPlace("CITY_PLACE_CREATED", OperationType.CREATE, sub, p); }
    public void cityPlaceUpdated(String sub, CityPlaceView p) { cityPlace("CITY_PLACE_UPDATED", OperationType.UPDATE, sub, p); }
    public void cityPlaceDeleted(String sub, CityPlaceView p) { cityPlace("CITY_PLACE_DELETED", OperationType.DELETE, sub, p); }

    public void cityRouteCreated(String sub, CityRouteView r) { cityRoute("CITY_ROUTE_CREATED", OperationType.CREATE, sub, r); }
    public void cityRouteUpdated(String sub, CityRouteView r) { cityRoute("CITY_ROUTE_UPDATED", OperationType.UPDATE, sub, r); }
    public void cityRouteDeleted(String sub, CityRouteView r) { cityRoute("CITY_ROUTE_DELETED", OperationType.DELETE, sub, r); }

    public void publicSpaceCreated(String sub, PublicSpaceView s) { publicSpace("PUBLIC_SPACE_CREATED", OperationType.CREATE, sub, s); }
    public void publicSpaceUpdated(String sub, PublicSpaceView s) { publicSpace("PUBLIC_SPACE_UPDATED", OperationType.UPDATE, sub, s); }
    public void publicSpaceDeleted(String sub, PublicSpaceView s) { publicSpace("PUBLIC_SPACE_DELETED", OperationType.DELETE, sub, s); }

    public void reservableResourceCreated(String sub, ReservableResourceView r) { resource("RESERVABLE_RESOURCE_CREATED", OperationType.CREATE, sub, r); }
    public void reservableResourceUpdated(String sub, ReservableResourceView r) { resource("RESERVABLE_RESOURCE_UPDATED", OperationType.UPDATE, sub, r); }
    public void reservableResourceDeleted(String sub, ReservableResourceView r) { resource("RESERVABLE_RESOURCE_DELETED", OperationType.DELETE, sub, r); }

    public void spaceReservationCreated(String citizenSub, SpaceReservationView r) {
        record("SPACE_RESERVATION_CREATED", OperationType.CREATE, citizenSub, CITIZEN_ROLE,
                null, null, RES_SPACE_RESERVATION, String.valueOf(r.getId()), reservationPayload(r));
    }

    public void spaceReservationDeleted(String sub, SpaceReservationView r) {
        record("SPACE_RESERVATION_DELETED", OperationType.DELETE, sub, null,
                null, null, RES_SPACE_RESERVATION, String.valueOf(r.getId()), reservationPayload(r));
    }

    public void eventCreated(String sub, EventView e) { event("EVENT_CREATED", OperationType.CREATE, sub, e); }
    public void eventUpdated(String sub, EventView e) { event("EVENT_UPDATED", OperationType.UPDATE, sub, e); }

    public void eventDeleted(String sub, EventView e, int ticketsRefundedCount, BigDecimal totalRefundedAmount) {
        record("EVENT_DELETED", OperationType.DELETE, sub, null,
                null, null, RES_EVENT, String.valueOf(e.getId()),
                payload("eventId", e.getId(), "placeId", e.getPlaceId(), "name", e.getName(),
                        "ticketsRefundedCount", ticketsRefundedCount,
                        "totalRefundedAmount", totalRefundedAmount, "currency", e.getCurrency()));
    }

    public void eventTicketPurchased(EventTicketView t) {
        record("EVENT_TICKET_PURCHASED", OperationType.CREATE, t.getCitizenSub(), CITIZEN_ROLE,
                null, null, RES_EVENT_TICKET, String.valueOf(t.getId()),
                payload("ticketId", t.getId(), "eventId", t.getEventId(), "citizenSub", t.getCitizenSub(),
                        "citizenName", t.getCitizenName(), "pricePaid", t.getPricePaid(),
                        "currency", t.getCurrency()));
    }

    /** Stripe webhook outcome — actor is the citizen who owns the ticket. */
    public void eventTicketStatusChanged(EventTicketView t, TicketStatus newStatus, String stripeEventType) {
        String eventType = newStatus == TicketStatus.PAID ? "EVENT_TICKET_CONFIRMED" : "EVENT_TICKET_CANCELLED";
        record(eventType, OperationType.UPDATE, t.getCitizenSub(), CITIZEN_ROLE,
                null, null, RES_EVENT_TICKET, String.valueOf(t.getId()),
                payload("ticketId", t.getId(), "eventId", t.getEventId(), "citizenSub", t.getCitizenSub(),
                        "previousStatus", "PENDING", "newStatus", newStatus.name(), "stripeEventType", stripeEventType));
    }

    public void eventTicketRefunded(String issuedBySub, EventTicketView t, Long refundId, BigDecimal amount, String reason) {
        record("EVENT_TICKET_REFUNDED", OperationType.UPDATE, issuedBySub, null,
                null, null, RES_EVENT_TICKET, String.valueOf(t.getId()),
                payload("ticketId", t.getId(), "eventId", t.getEventId(), "refundId", refundId,
                        "citizenSub", t.getCitizenSub(), "amount", amount, "currency", t.getCurrency(),
                        "reason", reason, "automatic", false));
    }

    public void eventTicketAutoRefunded(String adminSub, EventTicketView t, BigDecimal amount, String reason) {
        record("EVENT_TICKET_AUTO_REFUNDED", OperationType.UPDATE, adminSub, null,
                null, null, RES_EVENT_TICKET, String.valueOf(t.getId()),
                payload("ticketId", t.getId(), "eventId", t.getEventId(), "citizenSub", t.getCitizenSub(),
                        "amount", amount, "currency", t.getCurrency(), "reason", reason, "automatic", true));
    }

    private void cityPlace(String eventType, OperationType op, String sub, CityPlaceView p) {
        record(eventType, op, sub, null, null, null, RES_CITY_PLACE, String.valueOf(p.getId()),
                payload("placeId", p.getId(), "name", p.getName(), "latitude", p.getLatitude(),
                        "longitude", p.getLongitude(), "category", p.getCategory()));
    }

    private void cityRoute(String eventType, OperationType op, String sub, CityRouteView r) {
        List<Long> placeIds = r.getRoutePlaces() == null ? List.of()
                : r.getRoutePlaces().stream().map(rp -> rp.getPlace().getId()).toList();
        record(eventType, op, sub, null, null, null, RES_CITY_ROUTE, String.valueOf(r.getId()),
                payload("routeId", r.getId(), "name", r.getName(), "targetAudience", r.getTargetAudience(),
                        "placeIds", placeIds, "estimatedDurationMinutes", r.getEstimatedDurationMinutes()));
    }

    private void publicSpace(String eventType, OperationType op, String sub, PublicSpaceView s) {
        record(eventType, op, sub, null, null, null, RES_PUBLIC_SPACE, String.valueOf(s.getId()),
                payload("publicSpaceId", s.getId(), "name", s.getName(), "address", s.getAddress(),
                        "latitude", s.getLatitude(), "longitude", s.getLongitude()));
    }

    private void resource(String eventType, OperationType op, String sub, ReservableResourceView r) {
        record(eventType, op, sub, null, null, null, RES_RESERVABLE_RESOURCE, String.valueOf(r.getId()),
                payload("resourceId", r.getId(), "publicSpaceId", r.getPublicSpaceId(), "name", r.getName(),
                        "resourceType", r.getResourceType()));
    }

    private void event(String eventType, OperationType op, String sub, EventView e) {
        record(eventType, op, sub, null, null, null, RES_EVENT, String.valueOf(e.getId()),
                payload("eventId", e.getId(), "placeId", e.getPlaceId(), "name", e.getName(),
                        "eventType", e.getEventType() == null ? null : e.getEventType().name(),
                        "requiresTicket", e.isRequiresTicket(), "paid", e.isPaid(), "price", e.getPrice(),
                        "currency", e.getCurrency(), "capacity", e.getCapacity(),
                        "startsAt", e.getStartsAt() == null ? null : e.getStartsAt().toString(),
                        "endsAt", e.getEndsAt() == null ? null : e.getEndsAt().toString()));
    }

    private static Object reservationPayload(SpaceReservationView r) {
        return payload(
                "reservationId", r.getId(),
                "resourceId", r.getResourceId(),
                "citizenSub", r.getCitizenSub(),
                "citizenName", r.getCitizenName(),
                "reservationDate", r.getReservationDate() == null ? null : r.getReservationDate().toString(),
                "startTime", r.getStartTime() == null ? null : r.getStartTime().toString(),
                "endTime", r.getEndTime() == null ? null : r.getEndTime().toString());
    }
}
