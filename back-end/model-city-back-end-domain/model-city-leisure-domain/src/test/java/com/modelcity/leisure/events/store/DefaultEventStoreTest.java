package com.modelcity.leisure.events.store;

import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.repository.EventRepository;
import com.modelcity.leisure.events.repository.model.Event;
import com.modelcity.leisure.events.repository.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultEventStoreTest {

    @Mock
    EventRepository<Event> eventRepository;

    DefaultEventStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultEventStore(eventRepository);
    }

    private EventRequestDto buildRequest() {
        EventRequestDto request = new EventRequestDto();
        request.setPlaceId(1L);
        request.setName(Map.of("es", "Festival", "en", "Festival"));
        request.setDescription(Map.of("es", "Descripción"));
        request.setEventType(EventType.MUSIC);
        request.setRequiresTicket(true);
        request.setPaid(true);
        request.setPrice(BigDecimal.TEN);
        request.setCurrency("eur");
        request.setCapacity(100);
        request.setStartsAt(LocalDateTime.now().plusDays(1));
        request.setEndsAt(LocalDateTime.now().plusDays(1).plusHours(3));
        request.setPhotoUrls(List.of("p1.jpg"));
        return request;
    }

    @Test
    void create_setsActiveTrueAndUppercasesCurrency() {
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = store.create(buildRequest(), "price_123");

        assertThat(result.isActive()).isTrue();
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getStripePriceId()).isEqualTo("price_123");
        assertThat(result.getName()).isEqualTo("Festival");
    }

    @Test
    void create_withoutStripePrice_leavesStripePriceIdNull() {
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = store.create(buildRequest(), null);

        assertThat(result.getStripePriceId()).isNull();
    }

    @Test
    void update_activeEvent_appliesFields() {
        Event existing = new Event();
        when(eventRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = store.update(1L, buildRequest());

        assertThat(result.getName()).isEqualTo("Festival");
    }

    @Test
    void update_notFoundOrInactive_throwsResourceNotFound() {
        when(eventRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.update(99L, buildRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_marksInactive() {
        Event existing = new Event();
        existing.setActive(true);
        when(eventRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        store.softDelete(1L);

        assertThat(existing.isActive()).isFalse();
        verify(eventRepository).save(existing);
    }

    @Test
    void softDelete_notFound_throwsResourceNotFound() {
        when(eventRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.softDelete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findActiveById_delegatesToRepository() {
        store.findActiveById(1L);
        verify(eventRepository).findByIdAndActiveTrue(1L);
    }

    @Test
    void search_delegatesToRepository() {
        LocalDateTime now = LocalDateTime.now();
        store.search(EventType.MUSIC, true, now, null);
        verify(eventRepository).search(EventType.MUSIC, true, now, null);
    }
}
