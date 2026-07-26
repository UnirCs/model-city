package com.modelcity.leisure.events.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.common.util.PhotoUrls;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.repository.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Full detail of an event, with its localizable fields resolved to the requested locale.
 *
 * <p>Extensible DTO: a local deployment may subclass it to add city-specific fields and have its overridden
 * use cases / controllers work with the subtype through the generic seams (e.g. {@code GetEventUseCase<T extends EventDto>}).
 * It is a plain class (not a {@code record}) precisely so it can be extended.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {

    private Long id;
    private Long placeId;
    private String name;
    private String description;
    private EventType eventType;
    private boolean requiresTicket;
    private boolean paid;
    private BigDecimal price;
    private String currency;
    private Integer capacity;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String stripePriceId;
    private List<String> photoUrls;
    private boolean acquired;
    private Map<String, Map<String, String>> translations;

    public static EventDto from(EventView e, String locale) {
        return build(e, locale, false);
    }

    public static EventDto fromWithTranslations(EventView e, String locale) {
        return build(e, locale, true);
    }

    private static EventDto build(EventView e, String locale, boolean includeTranslations) {
        EventView.Translation t = e.getTranslations().get(locale);
        List<String> photos = PhotoUrls.collect(e.getPhotoUrl1(), e.getPhotoUrl2(), e.getPhotoUrl3());
        return new EventDto(e.getId(), e.getPlaceId(),
                LocalizedText.resolve(e.getName(), t == null ? null : t.getName()),
                LocalizedText.resolve(e.getDescription(), t == null ? null : t.getDescription()),
                e.getEventType(), e.isRequiresTicket(), e.isPaid(), e.getPrice(), e.getCurrency(),
                e.getCapacity(), e.getStartsAt(), e.getEndsAt(), e.getStripePriceId(), photos, false,
                includeTranslations ? allTranslations(e) : null);
    }

    /** Returns a copy with the per-user {@code acquired} flag resolved outside the shared cache. */
    public EventDto withAcquired(boolean acquired) {
        return new EventDto(id, placeId, name, description, eventType, requiresTicket, paid, price,
                currency, capacity, startsAt, endsAt, stripePriceId, photoUrls, acquired, translations);
    }

    private static Map<String, Map<String, String>> allTranslations(EventView e) {
        Map<String, Map<String, String>> all = new LinkedHashMap<>();
        all.put("name", localeMap(e.getName(), EventView.Translation::getName, e));
        all.put("description", localeMap(e.getDescription(), EventView.Translation::getDescription, e));
        return all;
    }

    private static Map<String, String> localeMap(String base,
                                                 Function<EventView.Translation, String> field,
                                                 EventView e) {
        return LocalizedText.buildLocaleMap(base, e.getTranslations(), field);
    }
}
