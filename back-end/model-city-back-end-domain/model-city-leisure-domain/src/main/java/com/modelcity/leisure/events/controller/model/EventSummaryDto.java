package com.modelcity.leisure.events.controller.model;

import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.repository.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight projection of an event for paginated listings, with its name resolved to the requested locale.
 *
 * <p>Extensible DTO (plain class, not a {@code record}): a city may subclass it to add fields and work with
 * the subtype through the generic seams (e.g. {@code GetEventsUseCase<T extends EventSummaryDto>}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryDto {

    private Long id;
    private Long placeId;
    private String name;
    private EventType eventType;
    private boolean requiresTicket;
    private boolean paid;
    private BigDecimal price;
    private String currency;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String stripePriceId;
    private String photoUrl;

    public static EventSummaryDto from(EventView e, String locale) {
        EventView.Translation t = e.getTranslations().get(locale);
        return new EventSummaryDto(e.getId(), e.getPlaceId(),
                LocalizedText.resolve(e.getName(), t == null ? null : t.getName()),
                e.getEventType(), e.isRequiresTicket(), e.isPaid(), e.getPrice(), e.getCurrency(),
                e.getStartsAt(), e.getEndsAt(), e.getStripePriceId(), e.getPhotoUrl1());
    }
}
