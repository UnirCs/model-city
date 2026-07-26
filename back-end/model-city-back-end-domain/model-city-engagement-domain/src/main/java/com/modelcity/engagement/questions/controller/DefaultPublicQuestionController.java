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
import com.modelcity.common.extensibility.ModelCityDisabledIfInherited;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default concrete {@link PublicQuestionController}. The component-scanned platform default; disabled at
 * startup when a local deployment provides its own bean for the seam.
 */
@RestController
@ModelCityDisabledIfInherited
public class DefaultPublicQuestionController extends PublicQuestionController<CivicQuestionSummaryDto, CivicQuestionDetailDto, CivicQuestionRequestDto, AnswerResponseDto, SubmitAnswerRequestDto> {

    public DefaultPublicQuestionController(
            GetPublicQuestionsUseCase<CivicQuestionSummaryDto> getPublicQuestionsUseCase,
            GetPublicQuestionUseCase<CivicQuestionDetailDto> getPublicQuestionUseCase,
            GetPublicQuestionForEditUseCase<CivicQuestionDetailDto> getPublicQuestionForEditUseCase,
            SubmitAnswerUseCase<AnswerResponseDto, SubmitAnswerRequestDto> submitAnswerUseCase,
            CreatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> createPublicQuestionUseCase,
            UpdatePublicQuestionUseCase<CivicQuestionDetailDto, CivicQuestionRequestDto> updatePublicQuestionUseCase,
            PatchPublicQuestionUseCase<CivicQuestionDetailDto> patchPublicQuestionUseCase) {
        super(getPublicQuestionsUseCase, getPublicQuestionUseCase, getPublicQuestionForEditUseCase,
                submitAnswerUseCase, createPublicQuestionUseCase, updatePublicQuestionUseCase,
                patchPublicQuestionUseCase);
    }
}
