package com.modelcity.engagement;

import com.modelcity.engagement.alerts.controller.DefaultSecurityAlertController;
import com.modelcity.engagement.alerts.controller.SecurityAlertController;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertDto;
import com.modelcity.engagement.alerts.controller.model.SecurityAlertRequestDto;
import com.modelcity.engagement.alerts.store.SecurityAlertStore;
import com.modelcity.engagement.alerts.usecase.CreateSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.DefaultCreateSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.DefaultDeleteSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.DefaultGetSecurityAlertsUseCase;
import com.modelcity.engagement.alerts.usecase.DeleteSecurityAlertUseCase;
import com.modelcity.engagement.alerts.usecase.GetSecurityAlertsUseCase;
import com.modelcity.engagement.trails.SystemTrailGenerator;
import com.modelcity.engagement.trails.controller.DefaultSystemTrailController;
import com.modelcity.engagement.trails.controller.SystemTrailController;
import com.modelcity.engagement.trails.store.EngagementSystemTrailStore;
import com.modelcity.engagement.trails.usecase.DefaultGetSystemTrailsUseCase;
import com.modelcity.engagement.trails.usecase.GetSystemTrailsUseCase;
import com.modelcity.engagement.questions.controller.DefaultPublicQuestionController;
import com.modelcity.engagement.questions.controller.PublicQuestionController;
import com.modelcity.engagement.questions.controller.model.AnswerResponseDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionDetailDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionRequestDto;
import com.modelcity.engagement.questions.controller.model.CivicQuestionSummaryDto;
import com.modelcity.engagement.questions.controller.model.SubmitAnswerRequestDto;
import com.modelcity.engagement.questions.store.AnswerStore;
import com.modelcity.engagement.questions.store.CivicQuestionStore;
import com.modelcity.engagement.questions.usecase.CachedCivicQuestionReader;
import com.modelcity.engagement.questions.usecase.CreatePublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.DefaultCreatePublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.DefaultGetPublicQuestionForEditUseCase;
import com.modelcity.engagement.questions.usecase.DefaultGetPublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.DefaultGetPublicQuestionsUseCase;
import com.modelcity.engagement.questions.usecase.DefaultPatchPublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.DefaultSubmitAnswerUseCase;
import com.modelcity.engagement.questions.usecase.DefaultUpdatePublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.GetPublicQuestionForEditUseCase;
import com.modelcity.engagement.questions.usecase.GetPublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.GetPublicQuestionsUseCase;
import com.modelcity.engagement.questions.usecase.PatchPublicQuestionUseCase;
import com.modelcity.engagement.questions.usecase.SubmitAnswerUseCase;
import com.modelcity.engagement.questions.usecase.UpdatePublicQuestionUseCase;
import com.modelcity.common.client.CoreClient;
import com.modelcity.common.config.ModelCityExtensibilityAutoConfiguration;
import com.modelcity.common.i18n.SupportedLocale;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies the engagement extension points under the scanned-defaults model: the {@code Default*} beans carry a
 * regular stereotype plus {@code @ModelCityDisabledIfInherited} and are registered by component scanning (here
 * simulated with {@code withBean}). By default the platform's {@code Default*} beans are wired, and as soon as a
 * local deployment declares its own use case or controller bean for the same seam, the matching default is
 * removed at startup by
 * {@link com.modelcity.common.extensibility.ModelCityDisabledIfInheritedProcessor} (registered by
 * {@link ModelCityExtensibilityAutoConfiguration}).
 */
class EngagementExtensionPointsTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ModelCityExtensibilityAutoConfiguration.class))
            .withBean(SecurityAlertStore.class, () -> mock(SecurityAlertStore.class))
            .withBean(CivicQuestionStore.class, () -> mock(CivicQuestionStore.class))
            .withBean(AnswerStore.class, () -> mock(AnswerStore.class))
            .withBean(CachedCivicQuestionReader.class, () -> mock(CachedCivicQuestionReader.class))
            .withBean(CoreClient.class, () -> mock(CoreClient.class))
            .withBean(SystemTrailGenerator.class, () -> mock(SystemTrailGenerator.class))
            .withBean(EngagementSystemTrailStore.class, () -> mock(EngagementSystemTrailStore.class))
            .withBean(SupportedLocale.class, () -> mock(SupportedLocale.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
            // alerts
            .withBean(DefaultGetSecurityAlertsUseCase.class)
            .withBean(DefaultCreateSecurityAlertUseCase.class)
            .withBean(DefaultDeleteSecurityAlertUseCase.class)
            .withBean(DefaultSecurityAlertController.class)
            // system trails
            .withBean(DefaultGetSystemTrailsUseCase.class)
            .withBean(DefaultSystemTrailController.class)
            // questions
            .withBean(DefaultGetPublicQuestionsUseCase.class)
            .withBean(DefaultGetPublicQuestionUseCase.class)
            .withBean(DefaultGetPublicQuestionForEditUseCase.class)
            .withBean(DefaultCreatePublicQuestionUseCase.class)
            .withBean(DefaultUpdatePublicQuestionUseCase.class)
            .withBean(DefaultPatchPublicQuestionUseCase.class)
            .withBean(DefaultSubmitAnswerUseCase.class)
            .withBean(DefaultPublicQuestionController.class);

    // --- alerts ---

    @Test
    void alertsPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetSecurityAlertsUseCase.class);
            assertThat(context).hasSingleBean(CreateSecurityAlertUseCase.class);
            assertThat(context).hasSingleBean(DeleteSecurityAlertUseCase.class);
            assertThat(context).hasSingleBean(SecurityAlertController.class);
            assertThat(context.getBean(GetSecurityAlertsUseCase.class)).isInstanceOf(DefaultGetSecurityAlertsUseCase.class);
            assertThat(context.getBean(CreateSecurityAlertUseCase.class)).isInstanceOf(DefaultCreateSecurityAlertUseCase.class);
            assertThat(context.getBean(DeleteSecurityAlertUseCase.class)).isInstanceOf(DefaultDeleteSecurityAlertUseCase.class);
            assertThat(context.getBean(SecurityAlertController.class)).isInstanceOf(DefaultSecurityAlertController.class);
        });
    }

    @Test
    void alertsUseCaseOverrideWins() {
        runner.withUserConfiguration(AlertsUseCaseOverride.class).run(context -> {
            assertThat(context).hasSingleBean(CreateSecurityAlertUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultCreateSecurityAlertUseCase.class);
            assertThat(context.getBean(CreateSecurityAlertUseCase.class)).isInstanceOf(AlertsUseCaseOverride.CustomCreateSecurityAlert.class);
        });
    }

    @Test
    void alertsControllerOverrideWins() {
        runner.withUserConfiguration(AlertsControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(SecurityAlertController.class);
            assertThat(context).doesNotHaveBean(DefaultSecurityAlertController.class);
            assertThat(context.getBean(SecurityAlertController.class)).isInstanceOf(AlertsControllerOverride.CustomSecurityAlertController.class);
        });
    }

    // --- system trails ---

    @Test
    void systemEventsPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetSystemTrailsUseCase.class);
            assertThat(context).hasSingleBean(SystemTrailController.class);
            assertThat(context.getBean(GetSystemTrailsUseCase.class)).isInstanceOf(DefaultGetSystemTrailsUseCase.class);
            assertThat(context.getBean(SystemTrailController.class)).isInstanceOf(DefaultSystemTrailController.class);
        });
    }

    // --- questions ---

    @Test
    void questionsPlatformDefaultsAreWired() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GetPublicQuestionsUseCase.class);
            assertThat(context).hasSingleBean(GetPublicQuestionUseCase.class);
            assertThat(context).hasSingleBean(GetPublicQuestionForEditUseCase.class);
            assertThat(context).hasSingleBean(CreatePublicQuestionUseCase.class);
            assertThat(context).hasSingleBean(UpdatePublicQuestionUseCase.class);
            assertThat(context).hasSingleBean(PatchPublicQuestionUseCase.class);
            assertThat(context).hasSingleBean(SubmitAnswerUseCase.class);
            assertThat(context).hasSingleBean(PublicQuestionController.class);
            assertThat(context.getBean(GetPublicQuestionsUseCase.class)).isInstanceOf(DefaultGetPublicQuestionsUseCase.class);
            assertThat(context.getBean(CreatePublicQuestionUseCase.class)).isInstanceOf(DefaultCreatePublicQuestionUseCase.class);
            assertThat(context.getBean(PublicQuestionController.class)).isInstanceOf(DefaultPublicQuestionController.class);
        });
    }

    @Test
    void questionsUseCaseOverrideWins() {
        runner.withUserConfiguration(QuestionsUseCaseOverride.class).run(context -> {
            assertThat(context).hasSingleBean(CreatePublicQuestionUseCase.class);
            assertThat(context).doesNotHaveBean(DefaultCreatePublicQuestionUseCase.class);
            assertThat(context.getBean(CreatePublicQuestionUseCase.class)).isInstanceOf(QuestionsUseCaseOverride.CustomCreatePublicQuestion.class);
        });
    }

    @Test
    void questionsControllerOverrideWins() {
        runner.withUserConfiguration(QuestionsControllerOverride.class).run(context -> {
            assertThat(context).hasSingleBean(PublicQuestionController.class);
            assertThat(context).doesNotHaveBean(DefaultPublicQuestionController.class);
            assertThat(context.getBean(PublicQuestionController.class)).isInstanceOf(QuestionsControllerOverride.CustomPublicQuestionController.class);
        });
    }

    // --- override configurations ---

    @Configuration
    static class AlertsUseCaseOverride {
        @Bean
        CreateSecurityAlertUseCase<SecurityAlertDto, SecurityAlertRequestDto> customCreateSecurityAlert() {
            return new CustomCreateSecurityAlert();
        }

        static class CustomCreateSecurityAlert implements CreateSecurityAlertUseCase<SecurityAlertDto, SecurityAlertRequestDto> {
            @Override
            public SecurityAlertDto execute(String sub, SecurityAlertRequestDto request, String locale) {
                return new SecurityAlertDto();
            }
        }
    }

    @Configuration
    static class AlertsControllerOverride {
        @Bean
        SecurityAlertController<SecurityAlertDto, SecurityAlertRequestDto> customSecurityAlertController(
                GetSecurityAlertsUseCase<SecurityAlertDto> getSecurityAlertsUseCase,
                CreateSecurityAlertUseCase<SecurityAlertDto, SecurityAlertRequestDto> createSecurityAlertUseCase,
                DeleteSecurityAlertUseCase deleteSecurityAlertUseCase) {
            return new CustomSecurityAlertController(getSecurityAlertsUseCase, createSecurityAlertUseCase, deleteSecurityAlertUseCase);
        }

        static class CustomSecurityAlertController extends SecurityAlertController<SecurityAlertDto, SecurityAlertRequestDto> {
            CustomSecurityAlertController(
                    GetSecurityAlertsUseCase<SecurityAlertDto> getSecurityAlertsUseCase,
                    CreateSecurityAlertUseCase<SecurityAlertDto, SecurityAlertRequestDto> createSecurityAlertUseCase,
                    DeleteSecurityAlertUseCase deleteSecurityAlertUseCase) {
                super(getSecurityAlertsUseCase, createSecurityAlertUseCase, deleteSecurityAlertUseCase);
            }
        }
    }

    @Configuration
    static class QuestionsUseCaseOverride {
        @Bean
        CreatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> customCreatePublicQuestion() {
            return new CustomCreatePublicQuestion();
        }

        static class CustomCreatePublicQuestion implements CreatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> {
            @Override
            public CivicQuestionDetailDto execute(String sub, CivicQuestionRequestDto request, String locale) {
                return new CivicQuestionDetailDto();
            }
        }
    }

    @Configuration
    static class QuestionsControllerOverride {
        @Bean
        PublicQuestionController<CivicQuestionSummaryDto, CivicQuestionDetailDto, CivicQuestionRequestDto, AnswerResponseDto, SubmitAnswerRequestDto> customPublicQuestionController(
                GetPublicQuestionsUseCase<CivicQuestionSummaryDto> getPublicQuestionsUseCase,
                GetPublicQuestionUseCase<CivicQuestionDetailDto> getPublicQuestionUseCase,
                GetPublicQuestionForEditUseCase<CivicQuestionDetailDto> getPublicQuestionForEditUseCase,
                SubmitAnswerUseCase<AnswerResponseDto, SubmitAnswerRequestDto> submitAnswerUseCase,
                CreatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> createPublicQuestionUseCase,
                UpdatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> updatePublicQuestionUseCase,
                PatchPublicQuestionUseCase<CivicQuestionDetailDto> patchPublicQuestionUseCase) {
            return new CustomPublicQuestionController(getPublicQuestionsUseCase, getPublicQuestionUseCase, getPublicQuestionForEditUseCase,
                    submitAnswerUseCase, createPublicQuestionUseCase, updatePublicQuestionUseCase, patchPublicQuestionUseCase);
        }

        static class CustomPublicQuestionController extends PublicQuestionController<CivicQuestionSummaryDto, CivicQuestionDetailDto, CivicQuestionRequestDto, AnswerResponseDto, SubmitAnswerRequestDto> {
            CustomPublicQuestionController(
                    GetPublicQuestionsUseCase<CivicQuestionSummaryDto> getPublicQuestionsUseCase,
                    GetPublicQuestionUseCase<CivicQuestionDetailDto> getPublicQuestionUseCase,
                    GetPublicQuestionForEditUseCase<CivicQuestionDetailDto> getPublicQuestionForEditUseCase,
                    SubmitAnswerUseCase<AnswerResponseDto, SubmitAnswerRequestDto> submitAnswerUseCase,
                    CreatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> createPublicQuestionUseCase,
                    UpdatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> updatePublicQuestionUseCase,
                    PatchPublicQuestionUseCase<CivicQuestionDetailDto> patchPublicQuestionUseCase) {
                super(getPublicQuestionsUseCase, getPublicQuestionUseCase, getPublicQuestionForEditUseCase,
                        submitAnswerUseCase, createPublicQuestionUseCase, updatePublicQuestionUseCase, patchPublicQuestionUseCase);
            }
        }
    }
}
