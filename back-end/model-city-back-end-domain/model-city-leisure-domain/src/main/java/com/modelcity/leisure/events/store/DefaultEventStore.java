package com.modelcity.leisure.events.store;

import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.stereotype.Component;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.common.i18n.LocalizedText;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.events.repository.EventRepository;
import com.modelcity.leisure.events.repository.model.Event;
import com.modelcity.leisure.events.repository.model.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** JPA adapter for the event persistence port. */
@RequiredArgsConstructor
@Component
@ModelCityDisabledIfInherited
public class DefaultEventStore implements EventStore<Event, EventRequestDto> {

    private final EventRepository<Event> eventRepository;

    @Override
    public Optional<Event> findActiveById(Long id) {
        return eventRepository.findByIdAndActiveTrue(id);
    }

    @Override
    public Page<Event> search(EventType type, Boolean paid, LocalDateTime now, Pageable pageable) {
        return eventRepository.search(type, paid, now, pageable);
    }

    @Override
    public Event create(EventRequestDto request, String stripePriceId) {
        Event event = new Event();
        event.setActive(true);
        applyFields(event, request);
        event.setStripePriceId(stripePriceId);
        return eventRepository.save(event);
    }

    @Override
    public Event update(Long id, EventRequestDto request) {
        Event event = eventRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        applyFields(event, request);
        return eventRepository.save(event);
    }

    @Override
    public void softDelete(Long id) {
        Event event = eventRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
        event.setActive(false);
        eventRepository.save(event);
    }

    private void applyFields(Event event, EventRequestDto request) {
        event.setPlaceId(request.getPlaceId());
        event.setName(LocalizedText.requireDefault("name", request.getName()));
        event.setDescription(LocalizedText.requireDefault("description", request.getDescription()));
        event.setEventType(request.getEventType());
        event.setRequiresTicket(request.isRequiresTicket());
        event.setPaid(request.isPaid());
        event.setPrice(request.getPrice());
        event.setCurrency(request.getCurrency().toUpperCase());
        event.setCapacity(request.getCapacity());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        applyPhotos(event, request.getPhotoUrls());
        applyTranslations(event, request);
    }

    private void applyTranslations(Event event, EventRequestDto request) {
        Map<String, String> name = LocalizedText.nonDefault(request.getName());
        Map<String, String> description = LocalizedText.nonDefault(request.getDescription());

        Set<String> locales = new HashSet<>();
        locales.addAll(name.keySet());
        locales.addAll(description.keySet());

        Map<String, Event.EventI18n> translations = new HashMap<>();
        for (String locale : locales) {
            translations.put(locale, new Event.EventI18n(name.get(locale), description.get(locale)));
        }
        event.getTranslations().clear();
        event.getTranslations().putAll(translations);
    }

    private void applyPhotos(Event event, List<String> photos) {
        event.setPhotoUrl1(null);
        event.setPhotoUrl2(null);
        event.setPhotoUrl3(null);
        if (photos == null) return;
        if (photos.size() > 0) event.setPhotoUrl1(photos.get(0));
        if (photos.size() > 1) event.setPhotoUrl2(photos.get(1));
        if (photos.size() > 2) event.setPhotoUrl3(photos.get(2));
    }
}
