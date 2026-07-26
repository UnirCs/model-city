package com.modelcity.leisure.events.usecase;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.exception.ResourceNotFoundException;
import com.modelcity.leisure.events.controller.model.EventDto;
import com.modelcity.leisure.events.controller.model.EventRequestDto;
import com.modelcity.leisure.events.controller.model.EventSummaryDto;
import com.modelcity.leisure.events.controller.model.PurchaseTicketRequestDto;
import com.modelcity.leisure.events.controller.model.RefundDto;
import com.modelcity.leisure.events.controller.model.RefundRequestDto;
import com.modelcity.leisure.events.controller.model.TicketDto;
import com.modelcity.leisure.events.repository.model.EventType;
import com.modelcity.leisure.events.repository.model.TicketStatus;
import com.modelcity.leisure.events.store.EventRefundStore;
import com.modelcity.leisure.events.store.EventStore;
import com.modelcity.leisure.events.store.EventTicketStore;
import com.modelcity.leisure.events.store.model.EventRefundView;
import com.modelcity.leisure.events.store.model.EventTicketView;
import com.modelcity.leisure.events.store.model.EventView;
import com.modelcity.leisure.facade.StripeFacade;
import com.modelcity.leisure.trails.SystemTrailGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    EventStore<EventView, EventRequestDto> eventStore;

    @Mock
    @SuppressWarnings("unchecked")
    EventTicketStore<EventTicketView> eventTicketStore;

    @Mock
    @SuppressWarnings("unchecked")
    EventRefundStore<EventRefundView> eventRefundStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    @Mock
    StripeFacade stripeFacade;

    @Mock
    EventWriteValidator eventWriteValidator;

    @Mock
    CachedEventReader cachedEventReader;

    @Mock
    CoreClient coreClient;

    private EventView mockEventView(Long id, String name) {
        EventView view = mock(EventView.class);
        when(view.getId()).thenReturn(id);
        when(view.getName()).thenReturn(name);
        when(view.getTranslations()).thenReturn(Map.of());
        when(view.isRequiresTicket()).thenReturn(true);
        when(view.isPaid()).thenReturn(false);
        when(view.getPrice()).thenReturn(BigDecimal.ZERO);
        when(view.getCurrency()).thenReturn("EUR");
        when(view.getCapacity()).thenReturn(100);
        when(view.getStartsAt()).thenReturn(LocalDateTime.now().plusDays(1));
        return view;
    }

    private EventTicketView mockTicketView(Long id, Long eventId) {
        EventTicketView t = mock(EventTicketView.class);
        when(t.getId()).thenReturn(id);
        when(t.getEventId()).thenReturn(eventId);
        when(t.getCitizenSub()).thenReturn("citizen-sub");
        when(t.getPricePaid()).thenReturn(BigDecimal.ZERO);
        when(t.getCurrency()).thenReturn("EUR");
        when(t.getStatus()).thenReturn(TicketStatus.PURCHASED);
        return t;
    }

    @Nested
    class CreateEventTests {

        DefaultCreateEventUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreateEventUseCase(eventStore, eventWriteValidator, stripeFacade, systemTrailGenerator);
        }

        @Test
        void execute_freeEvent_createsAndReturnsDto() {
            EventRequestDto request = new EventRequestDto();
            request.setName(Map.of("es", "Festival de Otoño"));
            request.setPaid(false);
            request.setRequiresTicket(true);

            EventView view = mockEventView(1L, "Festival de Otoño");
            when(eventStore.create(request, null)).thenReturn(view);

            EventDto result = useCase.execute("sub-agent", request, "es");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Festival de Otoño");
            verify(eventWriteValidator).validate(request);
            verify(stripeFacade, never()).createPrice(any(), any(), any());
            verify(systemTrailGenerator).eventCreated("sub-agent", view);
        }

        @Test
        void execute_paidEvent_createsPriceAndReturnsDto() {
            EventRequestDto request = new EventRequestDto();
            request.setName(Map.of("es", "Concierto"));
            request.setPaid(true);
            request.setRequiresTicket(true);
            request.setPrice(BigDecimal.TEN);
            request.setCurrency("EUR");

            EventView view = mockEventView(1L, "Concierto");
            when(view.isPaid()).thenReturn(true);
            when(view.getPrice()).thenReturn(BigDecimal.TEN);
            when(view.getStripePriceId()).thenReturn("price_123");
            when(stripeFacade.createPrice("Concierto", BigDecimal.TEN, "EUR")).thenReturn("price_123");
            when(eventStore.create(request, "price_123")).thenReturn(view);

            EventDto result = useCase.execute("sub-agent", request, "es");

            assertThat(result.getId()).isEqualTo(1L);
            verify(stripeFacade).createPrice("Concierto", BigDecimal.TEN, "EUR");
        }
    }

    @Nested
    class DeleteEventTests {

        DefaultDeleteEventUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultDeleteEventUseCase(eventStore, eventTicketStore, eventRefundStore, systemTrailGenerator);
        }

        @Test
        void execute_deletesEventAndAutoRefundsOutstandingTickets() {
            EventView event = mockEventView(1L, "Festival");
            EventTicketView ticket = mockTicketView(10L, 1L);
            EventRefundView refund = mock(EventRefundView.class);

            when(eventStore.findActiveById(1L)).thenReturn(Optional.of(event));
            when(eventTicketStore.findByEventAndStatus(1L, TicketStatus.PURCHASED))
                    .thenReturn(List.of(ticket));
            when(eventRefundStore.create(anyLong(), any(), any(), any(), anyBoolean(), any()))
                    .thenReturn(refund);

            useCase.execute(1L, "sub-agent");

            verify(eventTicketStore).markRefunded(eq(10L), any(LocalDateTime.class));
            verify(eventStore).softDelete(1L);
            verify(systemTrailGenerator).eventDeleted(eq("sub-agent"), eq(event), eq(1), any());
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(eventStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub-agent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetEventTests {

        DefaultGetEventUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetEventUseCase(cachedEventReader, eventTicketStore);
        }

        @Test
        void execute_returnsEventWithAcquiredFlagTrue() {
            EventDto dto = new EventDto();
            dto.setId(1L);
            when(cachedEventReader.getById(1L, "es")).thenReturn(dto);
            when(eventTicketStore.existsByEventAndCitizenAndStatusIn(eq(1L), eq("citizen-sub"), anyList()))
                    .thenReturn(true);

            EventDto result = useCase.execute(1L, "citizen-sub", "es");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.isAcquired()).isTrue();
        }

        @Test
        void execute_withNullSub_returnsNotAcquired() {
            EventDto dto = new EventDto();
            dto.setId(1L);
            when(cachedEventReader.getById(1L, "es")).thenReturn(dto);

            EventDto result = useCase.execute(1L, null, "es");

            assertThat(result.isAcquired()).isFalse();
        }
    }

    @Nested
    class GetEventForEditTests {

        DefaultGetEventForEditUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetEventForEditUseCase(cachedEventReader);
        }

        @Test
        void execute_delegatesToCachedReader() {
            EventDto dto = new EventDto();
            dto.setId(1L);
            when(cachedEventReader.getForEdit(1L, "es")).thenReturn(dto);

            EventDto result = useCase.execute(1L, "es");

            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Nested
    class GetEventsTests {

        DefaultGetEventsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetEventsUseCase(eventStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsPageOfSummaries() {
            EventView view = mockEventView(1L, "Feria");
            when(view.getPhotoUrl1()).thenReturn(null);
            Page<EventView> page = new PageImpl<>(List.of(view));
            doReturn(page).when(eventStore).search(any(), any(), any(LocalDateTime.class), any(Pageable.class));

            Page<EventSummaryDto> result = useCase.execute(EventType.MUSIC, null, 0, "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    class UpdateEventTests {

        DefaultUpdateEventUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultUpdateEventUseCase(eventStore, eventWriteValidator, systemTrailGenerator);
        }

        @Test
        void execute_updatesAndReturnsDto() {
            EventRequestDto request = new EventRequestDto();
            request.setName(Map.of("es", "Festival Actualizado"));
            request.setPaid(false);

            EventView existing = mockEventView(1L, "Festival");
            EventView updated = mockEventView(1L, "Festival Actualizado");

            when(eventStore.findActiveById(1L)).thenReturn(Optional.of(existing));
            when(eventStore.update(1L, request)).thenReturn(updated);

            EventDto result = useCase.execute(1L, "sub-agent", request, "es");

            assertThat(result.getName()).isEqualTo("Festival Actualizado");
            verify(eventWriteValidator).validate(request);
            verify(systemTrailGenerator).eventUpdated("sub-agent", updated);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(eventStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub", new EventRequestDto(), "es"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetEventTicketsTests {

        DefaultGetEventTicketsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetEventTicketsUseCase(eventStore, eventTicketStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsTicketPage() {
            EventView event = mockEventView(1L, "Festival");
            EventTicketView ticket = mockTicketView(10L, 1L);
            Page<EventTicketView> page = new PageImpl<>(List.of(ticket));

            when(eventStore.findActiveById(1L)).thenReturn(Optional.of(event));
            doReturn(page).when(eventTicketStore).findByEvent(eq(1L), any(Pageable.class));

            Page<TicketDto> result = useCase.execute(1L, 0);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void execute_throwsWhenEventNotFound() {
            when(eventStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, 0))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class PurchaseTicketTests {

        DefaultPurchaseTicketUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultPurchaseTicketUseCase(eventStore, eventTicketStore, coreClient, systemTrailGenerator);
        }

        @Test
        void execute_freeEvent_purchasesDirectly() {
            EventView event = mockEventView(1L, "Festival Gratis");
            EventTicketView pending = mockTicketView(10L, 1L);
            EventTicketView purchased = mockTicketView(10L, 1L);

            PurchaseTicketRequestDto request = new PurchaseTicketRequestDto();

            when(eventStore.findActiveById(1L)).thenReturn(Optional.of(event));
            when(eventTicketStore.createPending(eq(1L), eq("citizen-sub"), any(), any(), any(), any()))
                    .thenReturn(pending);
            when(eventTicketStore.findById(10L)).thenReturn(Optional.of(purchased));

            TicketDto result = useCase.execute(1L, "citizen-sub", request);

            assertThat(result).isNotNull();
            verify(eventTicketStore).markStatus(10L, TicketStatus.PURCHASED);
        }

        @Test
        void execute_throwsWhenEventNotFound() {
            when(eventStore.findActiveById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "sub", new PurchaseTicketRequestDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void execute_throwsWhenEventDoesNotRequireTicket() {
            EventView event = mockEventView(1L, "Open Event");
            when(event.isRequiresTicket()).thenReturn(false);
            when(eventStore.findActiveById(1L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> useCase.execute(1L, "sub", new PurchaseTicketRequestDto()))
                    .isInstanceOf(ResponseStatusException.class);
        }

        @Test
        void execute_throwsWhenEventSoldOut() {
            EventView event = mockEventView(1L, "Lleno");
            when(event.getCapacity()).thenReturn(10);
            when(eventStore.findActiveById(1L)).thenReturn(Optional.of(event));
            when(eventTicketStore.countByEventAndStatusIn(eq(1L), anyCollection())).thenReturn(10L);

            assertThatThrownBy(() -> useCase.execute(1L, "sub", new PurchaseTicketRequestDto()))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class RefundTicketTests {

        DefaultRefundTicketUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultRefundTicketUseCase(eventTicketStore, eventRefundStore, stripeFacade, systemTrailGenerator);
        }

        @Test
        void execute_freeTicket_refundsWithoutStripe() {
            EventTicketView ticket = mockTicketView(10L, 1L);
            EventRefundView refund = mock(EventRefundView.class);
            when(refund.getId()).thenReturn(100L);
            when(refund.getAmount()).thenReturn(BigDecimal.ZERO);
            when(refund.getCurrency()).thenReturn("EUR");
            when(ticket.getPricePaid()).thenReturn(BigDecimal.ZERO);
            when(ticket.getStatus()).thenReturn(TicketStatus.PURCHASED);

            when(eventTicketStore.findById(10L)).thenReturn(Optional.of(ticket));
            when(eventRefundStore.create(anyLong(), any(), any(), any(), anyBoolean(), any()))
                    .thenReturn(refund);

            RefundDto result = useCase.execute(1L, 10L, "admin-sub", new RefundRequestDto());

            assertThat(result).isNotNull();
            verify(stripeFacade, never()).refund(any(), any(), any());
        }

        @Test
        void execute_throwsWhenTicketNotFound() {
            when(eventTicketStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(1L, 99L, "sub", null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void execute_throwsWhenAlreadyRefunded() {
            EventTicketView ticket = mockTicketView(10L, 1L);
            when(ticket.getStatus()).thenReturn(TicketStatus.REFUNDED);
            when(eventTicketStore.findById(10L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> useCase.execute(1L, 10L, "sub", null))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Nested
    class GetCitizenTicketsTests {

        DefaultGetCitizenTicketsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetCitizenTicketsUseCase(eventTicketStore, eventStore, coreClient);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_citizen_canViewOwnTickets() {
            EventTicketView ticket = mockTicketView(10L, 1L);
            EventView event = mockEventView(1L, "Festival");
            Page<EventTicketView> page = new PageImpl<>(List.of(ticket));

            doReturn(page).when(eventTicketStore).findByCitizen(eq("citizen-sub"), any(Pageable.class));
            when(eventStore.findActiveById(1L)).thenReturn(Optional.of(event));
            when(coreClient.getUserRole("citizen-sub")).thenReturn("MODEL-CITY-CITIZEN");

            Page<?> result = useCase.execute("citizen-sub", "citizen-sub", 0, null);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void execute_citizen_cannotViewOthersTickets() {
            when(coreClient.getUserRole("citizen-sub")).thenReturn("MODEL-CITY-CITIZEN");

            assertThatThrownBy(() -> useCase.execute("other-sub", "citizen-sub", 0, null))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }
}
