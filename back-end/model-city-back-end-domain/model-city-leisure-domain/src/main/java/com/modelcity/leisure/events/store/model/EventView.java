package com.modelcity.leisure.events.store.model;

import com.modelcity.leisure.events.repository.model.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/** Read-only view of an event, exposed by the persistence adapter. */
public interface EventView {
    Long getId();
    Long getPlaceId();
    String getName();
    String getDescription();
    EventType getEventType();
    boolean isRequiresTicket();
    boolean isPaid();
    BigDecimal getPrice();
    String getCurrency();
    Integer getCapacity();
    LocalDateTime getStartsAt();
    LocalDateTime getEndsAt();
    String getStripePriceId();
    String getPhotoUrl1();
    String getPhotoUrl2();
    String getPhotoUrl3();

    /** Per-locale translations of the localizable fields, keyed by language code. */
    Map<String, ? extends Translation> getTranslations();

    /** Localizable fields of an event for a single non-default locale. */
    interface Translation {
        String getName();
        String getDescription();
    }
}
