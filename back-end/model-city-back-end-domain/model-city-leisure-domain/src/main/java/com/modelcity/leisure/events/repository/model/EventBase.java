package com.modelcity.leisure.events.repository.model;

import com.modelcity.leisure.events.store.model.EventView;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Invariant columns of a cultural / leisure event, shared by the platform default entity ({@link Event}) and
 * any city-specific subclass. Kept as a {@code @MappedSuperclass} so a city that needs extra columns can
 * declare its own {@code @Entity} extending this base instead of forking it — see {@code EventRepository}'s
 * Javadoc.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class EventBase implements EventView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    /** Localizable fields below hold the default-locale (es) value; other locales live in {@link #translations}. */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private EventType eventType;

    @Column(name = "requires_ticket", nullable = false)
    private boolean requiresTicket;

    @Column(nullable = false)
    private boolean paid;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    /** Optional maximum number of tickets that can be sold. */
    private Integer capacity;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    /** Stripe Price ID for future payment integration. */
    @Column(name = "stripe_price_id", length = 255)
    private String stripePriceId;

    @Column(name = "photo_url_1", length = 2048)
    private String photoUrl1;

    @Column(name = "photo_url_2", length = 2048)
    private String photoUrl2;

    @Column(name = "photo_url_3", length = 2048)
    private String photoUrl3;

    /** Soft-delete flag. */
    @Column(nullable = false)
    private boolean active;

    /** Non-default-locale translations of the localizable fields, keyed by language code. */
    @ElementCollection
    @CollectionTable(name = "event_translations", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "locale")
    @BatchSize(size = 64)
    @Builder.Default
    private Map<String, EventI18n> translations = new HashMap<>();

    /** Localizable fields of an event for a single locale. */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventI18n implements EventView.Translation {
        @Column(name = "name")
        private String name;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
    }
}
