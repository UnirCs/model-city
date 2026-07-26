package com.modelcity.engagement.questions.controller;

import com.modelcity.engagement.questions.controller.model.AnswerResponseDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionSummaryDto;
import com.modelcity.engagement.questions.controller.model.SubmitAnswerRequestDto;
import com.modelcity.engagement.questions.usecase.CreatePublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.GetPublicQuestionForEditUseCase;
import com.modelcity.engagement.questions.usecase.GetPublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.GetPublicQuestionsUseCase;
import com.modelcity.engagement.questions.usecase.PatchPublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.SubmitAnswerUseCase;
import com.modelcity.engagement.questions.usecase.UpdatePublicQuestionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.Locale;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicQuestionControllerTest {

    @Mock GetPublicQuestionsUseCase<CivicQuestionSummaryDto> getPublicQuestionsUseCase;
    @Mock GetPublicQuestionUseCase<CivicQuestionDetailDto> getPublicQuestionUseCase;
    @Mock GetPublicQuestionForEditUseCase<CivicQuestionDetailDto> getPublicQuestionForEditUseCase;
    @Mock SubmitAnswerUseCase<AnswerResponseDto, SubmitAnswerRequestDto> submitAnswerUseCase;
    @Mock CreatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> createPublicQuestionUseCase;
    @Mock UpdatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> updatePublicQuestionUseCase;
    @Mock PatchPublicQuestionUseCase<CivicQuestionDetailDto> patchPublicQuestionUseCase;

    DefaultPublicQuestionController controller;

    @BeforeEach
    void setUp() {
        controller = new DefaultPublicQuestionController(getPublicQuestionsUseCase, getPublicQuestionUseCase,
                getPublicQuestionForEditUseCase, submitAnswerUseCase, createPublicQuestionUseCase,
                updatePublicQuestionUseCase, patchPublicQuestionUseCase);
    }

    @Test
    void getPublicQuestions_delegatesWithResolvedLocale() {
        controller.getPublicQuestions("active", 5L, 10L, 1, Locale.FRENCH);
        verify(getPublicQuestionsUseCase).execute("active", 5L, 10L, 1, "fr");
    }

    @Test
    void getPublicQuestion_withoutTranslations_usesGetUseCase() {
        controller.getPublicQuestion(1L, "citizen-sub", null, Locale.ENGLISH);
        verify(getPublicQuestionUseCase).execute(1L, "citizen-sub", "en");
        verify(getPublicQuestionForEditUseCase, never()).execute(any(), any());
    }

    @Test
    void getPublicQuestion_withFullTranslations_usesForEditUseCase() {
        controller.getPublicQuestion(1L, "citizen-sub", "full", Locale.ENGLISH);
        verify(getPublicQuestionForEditUseCase).execute(1L, "en");
        verify(getPublicQuestionUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void createPublicQuestion_delegatesToUseCase() {
        CivicQuestionRequestDto request = new CivicQuestionRequestDto();
        controller.createPublicQuestion("admin-sub", request, Locale.ENGLISH);
        verify(createPublicQuestionUseCase).execute("admin-sub", request, "en");
    }

    @Test
    void updatePublicQuestion_delegatesToUseCase() {
        CivicQuestionRequestDto request = new CivicQuestionRequestDto();
        controller.updatePublicQuestion(1L, "admin-sub", request, Locale.ENGLISH);
        verify(updatePublicQuestionUseCase).execute(1L, "admin-sub", request, "en");
    }

    @Test
    void patchPublicQuestion_delegatesToUseCase() {
        controller.patchPublicQuestion(1L, "admin-sub", "{}", Locale.ENGLISH);
        verify(patchPublicQuestionUseCase).execute(1L, "admin-sub", "{}", "en");
    }

    @Test
    void submitAnswer_delegatesToUseCase() {
        SubmitAnswerRequestDto request = new SubmitAnswerRequestDto(UUID.randomUUID(), "YES");
        controller.submitAnswer(1L, "citizen-sub", request);
        verify(submitAnswerUseCase).execute(1L, "citizen-sub", request);
    }
}
