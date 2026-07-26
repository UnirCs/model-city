package com.modelcity.engagement.questions.usecase;

import com.modelcity.common.client.CoreClient;
import com.modelcity.common.dto.OperationAuthorizationResponseDto;
import com.modelcity.engagement.questions.controller.model.AnswerResponseDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionSummaryDto;
import com.modelcity.engagement.questions.controller.model.SubmitAnswerRequestDto;
import com.modelcity.engagement.questions.store.AnswerStore;
import com.modelcity.engagement.questions.store.CivicQuestionStore;
import com.modelcity.engagement.questions.store.model.CivicQuestionView;
import com.modelcity.engagement.trails.SystemTrailGenerator;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CivicQuestionUseCasesTest {

    @Mock
    @SuppressWarnings("unchecked")
    CivicQuestionStore<CivicQuestionView, CivicQuestionRequestDto> civicQuestionStore;

    @Mock
    AnswerStore answerStore;

    @Mock
    SystemTrailGenerator systemTrailGenerator;

    @Mock
    CoreClient coreClient;

    @Mock
    CachedCivicQuestionReader cachedCivicQuestionReader;

    private CivicQuestionView mockQuestionView(Long id, String title) {
        CivicQuestionView view = mock(CivicQuestionView.class);
        when(view.getId()).thenReturn(id);
        when(view.getTitle()).thenReturn(title);
        when(view.getDescription()).thenReturn("Descripción");
        when(view.getImageUrl()).thenReturn(null);
        when(view.getOpenDate()).thenReturn(LocalDate.now().plusDays(5));
        when(view.getCloseDate()).thenReturn(LocalDate.now().plusDays(15));
        when(view.getZoneId()).thenReturn(1L);
        when(view.getNeighbourhoodId()).thenReturn(10L);
        when(view.getObjectives()).thenReturn(List.of());
        when(view.getYesCount()).thenReturn(0L);
        when(view.getNoCount()).thenReturn(0L);
        when(view.getTranslations()).thenReturn(Map.of());
        return view;
    }

    private CivicQuestionRequestDto buildRequest() {
        CivicQuestionRequestDto req = new CivicQuestionRequestDto();
        req.setTitle(Map.of("es", "¿Deberíamos añadir más zonas verdes?"));
        req.setDescription(Map.of("es", "Descripción detallada"));
        req.setOpenDate(LocalDate.now().plusDays(5));
        req.setCloseDate(LocalDate.now().plusDays(15));
        req.setZoneId(1L);
        req.setNeighbourhoodId(10L);
        return req;
    }

    // ===================== QUESTION USE CASES =====================

    @Nested
    class CreatePublicQuestionTests {

        DefaultCreatePublicQuestionUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultCreatePublicQuestionUseCase(civicQuestionStore, systemTrailGenerator);
        }

        @Test
        void execute_createsQuestionWithValidDates() {
            CivicQuestionView saved = mockQuestionView(1L, "¿Deberíamos añadir más zonas verdes?");
            CivicQuestionRequestDto request = buildRequest();

            when(civicQuestionStore.create(request)).thenReturn(saved);

            CivicQuestionDetailDto result = useCase.execute("agent-sub", request, "es");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("¿Deberíamos añadir más zonas verdes?");
            verify(systemTrailGenerator).civicQuestionCreated("agent-sub", saved);
        }

        @Test
        void execute_throwsWhenOpenDateNotFuture() {
            CivicQuestionRequestDto request = buildRequest();
            request.setOpenDate(LocalDate.now());

            assertThatThrownBy(() -> useCase.execute("agent-sub", request, "es"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Open date must be after today");
        }

        @Test
        void execute_throwsWhenCloseDateTooCloseTooOpenDate() {
            CivicQuestionRequestDto request = buildRequest();
            request.setCloseDate(request.getOpenDate().plusDays(1));

            assertThatThrownBy(() -> useCase.execute("agent-sub", request, "es"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("at least 3 days");
        }
    }

    @Nested
    class GetPublicQuestionTests {

        DefaultGetPublicQuestionUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetPublicQuestionUseCase(cachedCivicQuestionReader, answerStore);
        }

        @Test
        void execute_withSub_setsSubmittableWhenNotAnswered() {
            CivicQuestionDetailDto dto = new CivicQuestionDetailDto();
            dto.setId(1L);
            when(cachedCivicQuestionReader.getById(1L, "es")).thenReturn(dto);
            when(answerStore.hasAnswered(1L, "citizen-sub")).thenReturn(false);

            CivicQuestionDetailDto result = useCase.execute(1L, "citizen-sub", "es");

            assertThat(result.isSubmittable()).isTrue();
        }

        @Test
        void execute_withSub_setsNotSubmittableWhenAlreadyAnswered() {
            CivicQuestionDetailDto dto = new CivicQuestionDetailDto();
            dto.setId(1L);
            when(cachedCivicQuestionReader.getById(1L, "es")).thenReturn(dto);
            when(answerStore.hasAnswered(1L, "citizen-sub")).thenReturn(true);

            CivicQuestionDetailDto result = useCase.execute(1L, "citizen-sub", "es");

            assertThat(result.isSubmittable()).isFalse();
        }

        @Test
        void execute_withNullSub_setsNotSubmittable() {
            CivicQuestionDetailDto dto = new CivicQuestionDetailDto();
            dto.setId(1L);
            when(cachedCivicQuestionReader.getById(1L, "es")).thenReturn(dto);

            CivicQuestionDetailDto result = useCase.execute(1L, null, "es");

            assertThat(result.isSubmittable()).isFalse();
        }
    }

    @Nested
    class GetPublicQuestionForEditTests {

        DefaultGetPublicQuestionForEditUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetPublicQuestionForEditUseCase(cachedCivicQuestionReader);
        }

        @Test
        void execute_delegatesToCachedReader() {
            CivicQuestionDetailDto dto = new CivicQuestionDetailDto();
            dto.setId(1L);
            when(cachedCivicQuestionReader.getForEdit(1L, "es")).thenReturn(dto);

            CivicQuestionDetailDto result = useCase.execute(1L, "es");

            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Nested
    class GetPublicQuestionsTests {

        DefaultGetPublicQuestionsUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultGetPublicQuestionsUseCase(civicQuestionStore);
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_returnsPageOfSummaries() {
            CivicQuestionView view = mockQuestionView(1L, "Pregunta Activa");
            Page<CivicQuestionView> page = new PageImpl<>(List.of(view));
            doReturn(page).when(civicQuestionStore).search(any(), any(), any(), any(Pageable.class));

            Page<CivicQuestionSummaryDto> result = useCase.execute("active", null, null, 0, "es");

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Pregunta Activa");
        }

        @Test
        @SuppressWarnings("unchecked")
        void execute_withZoneFilter_passesItToStore() {
            Page<CivicQuestionView> emptyPage = new PageImpl<>(List.of());
            doReturn(emptyPage).when(civicQuestionStore).search(any(), eq(5L), any(), any(Pageable.class));

            Page<CivicQuestionSummaryDto> result = useCase.execute(null, 5L, null, 0, "es");

            verify(civicQuestionStore).search(any(), eq(5L), any(), any(Pageable.class));
        }
    }

    @Nested
    class UpdatePublicQuestionTests {

        DefaultUpdatePublicQuestionUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultUpdatePublicQuestionUseCase(civicQuestionStore, systemTrailGenerator);
        }

        @Test
        void execute_updatesFutureQuestion() {
            CivicQuestionView existing = mockQuestionView(1L, "Old");
            CivicQuestionView updated = mockQuestionView(1L, "Updated");
            CivicQuestionRequestDto request = buildRequest();

            when(civicQuestionStore.findById(1L)).thenReturn(Optional.of(existing));
            when(civicQuestionStore.update(1L, request)).thenReturn(updated);

            CivicQuestionDetailDto result = useCase.execute(1L, "agent-sub", request, "es");

            assertThat(result.getTitle()).isEqualTo("Updated");
            verify(systemTrailGenerator).civicQuestionUpdated("agent-sub", updated, "PUT");
        }

        @Test
        void execute_throwsWhenQuestionAlreadyOpen() {
            CivicQuestionView existing = mockQuestionView(1L, "Pregunta");
            when(existing.getOpenDate()).thenReturn(LocalDate.now().minusDays(1));
            when(civicQuestionStore.findById(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> useCase.execute(1L, "agent-sub", buildRequest(), "es"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Only future questions can be updated");
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(civicQuestionStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "agent-sub", buildRequest(), "es"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Question not found");
        }
    }

    @Nested
    class PatchPublicQuestionTests {

        DefaultPatchPublicQuestionUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultPatchPublicQuestionUseCase(civicQuestionStore, null, systemTrailGenerator);
        }

        @Test
        void execute_throwsWhenNotFound() {
            when(civicQuestionStore.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(99L, "agent-sub", "{}", "es"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Question not found");
        }
    }

    @Nested
    class SubmitAnswerTests {

        DefaultSubmitAnswerUseCase useCase;

        @BeforeEach
        void setUp() {
            useCase = new DefaultSubmitAnswerUseCase(coreClient, civicQuestionStore, answerStore, systemTrailGenerator);
        }

        private OperationAuthorizationResponseDto buildValidAuth(UUID uuid, Long questionId, String sub) {
            return new OperationAuthorizationResponseDto(
                    uuid,
                    "CONFIRM_ANSWER",
                    "public-question",
                    questionId.toString(),
                    sub,
                    Instant.now().plusSeconds(300),
                    "VERIFIED",
                    1,
                    Instant.now(),
                    "hashed-dni-123"
            );
        }

        @Test
        void execute_submitsAnswerSuccessfully() {
            UUID authId = UUID.randomUUID();
            OperationAuthorizationResponseDto auth = buildValidAuth(authId, 1L, "citizen-sub");
            CivicQuestionView question = mockQuestionView(1L, "Pregunta");
            SubmitAnswerRequestDto request = new SubmitAnswerRequestDto(authId, "YES");

            when(coreClient.getOperationAuthorization(authId)).thenReturn(auth);
            when(civicQuestionStore.findById(1L)).thenReturn(Optional.of(question));
            when(answerStore.hasVoted(1L, "hashed-dni-123")).thenReturn(false);
            when(answerStore.createAnswer(eq(1L), eq("citizen-sub"), eq("hashed-dni-123"), any())).thenReturn(100L);

            AnswerResponseDto result = useCase.execute(1L, "citizen-sub", request);

            assertThat(result.getAnswerId()).isEqualTo(100L);
            verify(coreClient).burnOperationAuthorization(authId);
        }

        @Test
        void execute_throwsConflictWhenAlreadyVoted() {
            UUID authId = UUID.randomUUID();
            OperationAuthorizationResponseDto auth = buildValidAuth(authId, 1L, "citizen-sub");
            CivicQuestionView question = mockQuestionView(1L, "Pregunta");
            SubmitAnswerRequestDto request = new SubmitAnswerRequestDto(authId, "YES");

            when(coreClient.getOperationAuthorization(authId)).thenReturn(auth);
            when(civicQuestionStore.findById(1L)).thenReturn(Optional.of(question));
            when(answerStore.hasVoted(1L, "hashed-dni-123")).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute(1L, "citizen-sub", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("already voted");
        }

        @Test
        void execute_throwsForbiddenWhenAuthBelongsToDifferentUser() {
            UUID authId = UUID.randomUUID();
            OperationAuthorizationResponseDto auth = buildValidAuth(authId, 1L, "other-sub");
            SubmitAnswerRequestDto request = new SubmitAnswerRequestDto(authId, "YES");

            when(coreClient.getOperationAuthorization(authId)).thenReturn(auth);

            assertThatThrownBy(() -> useCase.execute(1L, "citizen-sub", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("belongs to a different user");
        }

        @Test
        void execute_throwsWhenAuthNotVerified() {
            UUID authId = UUID.randomUUID();
            OperationAuthorizationResponseDto auth = new OperationAuthorizationResponseDto(
                    authId, "CONFIRM_ANSWER", "public-question", "1", "citizen-sub",
                    Instant.now().plusSeconds(300), "PENDING", 1, Instant.now(), "hash");
            SubmitAnswerRequestDto request = new SubmitAnswerRequestDto(authId, "YES");

            when(coreClient.getOperationAuthorization(authId)).thenReturn(auth);

            assertThatThrownBy(() -> useCase.execute(1L, "citizen-sub", request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("not in VERIFIED status");
        }
    }
}
