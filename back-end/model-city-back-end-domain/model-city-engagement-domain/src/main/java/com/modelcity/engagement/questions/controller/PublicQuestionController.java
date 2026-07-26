package com.modelcity.engagement.questions.controller;

import com.modelcity.common.extensibility.ModelCityExtensionPoint;
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
import com.modelcity.common.i18n.SupportedLocale;
import com.modelcity.common.security.ModelCityAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Civic questions endpoints.
 *
 * <p>Overridable base controller (abstract): the platform registers {@link DefaultPublicQuestionController}
 * as the default bean. A local deployment overrides by declaring its own {@code @RestController} that
 * {@code extends PublicQuestionController}; the default then backs off.
 */
@Slf4j
@RestController
@RequestMapping("/public-questions")
@RequiredArgsConstructor
@ModelCityExtensionPoint
public abstract class PublicQuestionController<S extends CivicQuestionSummaryDto, D extends CivicQuestionDetailDto, R extends CivicQuestionRequestDto, A extends AnswerResponseDto, SR extends SubmitAnswerRequestDto> {

    protected final GetPublicQuestionsUseCase<S> getPublicQuestionsUseCase;
    protected final GetPublicQuestionUseCase<D> getPublicQuestionUseCase;
    protected final GetPublicQuestionForEditUseCase<D> getPublicQuestionForEditUseCase;
    protected final SubmitAnswerUseCase<A, SR> submitAnswerUseCase;
    protected final CreatePublicQuestionUseCase<D, R> createPublicQuestionUseCase;
    protected final UpdatePublicQuestionUseCase<D, R> updatePublicQuestionUseCase;
    protected final PatchPublicQuestionUseCase<D> patchPublicQuestionUseCase;

    /** Lists civic questions with optional filters and pagination (3 per page). */
    @GetMapping
    public Page<S> getPublicQuestions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long neighbourhoodId,
            @RequestParam(defaultValue = "0") int page,
            Locale locale) {
        log.debug("GET /public-questions status={} zoneId={} neighbourhoodId={} page={}", status, zoneId, neighbourhoodId, page);
        return getPublicQuestionsUseCase.execute(status, zoneId, neighbourhoodId, page, SupportedLocale.from(locale).code());
    }

    /** Returns full detail of a single civic question. {@code ?translations=full} returns every locale (admin editing). */
    @GetMapping("/{id}")
    public D getPublicQuestion(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Sub", required = false) String sub,
            @RequestParam(required = false) String translations,
            Locale locale) {
        log.debug("GET /public-questions/{} sub={} translations={}", id, sub, translations);
        String code = SupportedLocale.from(locale).code();
        return "full".equals(translations)
                ? getPublicQuestionForEditUseCase.execute(id, code)
                : getPublicQuestionUseCase.execute(id, sub, code);
    }

    /** Creates a new civic question. Admin only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ModelCityAccess.PlatformAdmin
    public D createPublicQuestion(
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("POST /public-questions sub={}", sub);
        return createPublicQuestionUseCase.execute(sub, request, SupportedLocale.from(locale).code());
    }

    /** Fully replaces a future civic question. Admin or backoffice only. */
    @PutMapping("/{id}")
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public D updatePublicQuestion(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid R request,
            Locale locale) {
        log.debug("PUT /public-questions/{} sub={}", id, sub);
        return updatePublicQuestionUseCase.execute(id, sub, request, SupportedLocale.from(locale).code());
    }

    /** Partially updates a civic question via JSON Merge Patch. Admin or backoffice only. */
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ModelCityAccess.PlatformAdmin
    @ModelCityAccess.BackOffice
    public D patchPublicQuestion(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody String patchBody,
            Locale locale) {
        log.debug("PATCH /public-questions/{} sub={}", id, sub);
        return patchPublicQuestionUseCase.execute(id, sub, patchBody, SupportedLocale.from(locale).code());
    }

    /** Registers a YES/NO answer for a civic question after validating the operation authorization. */
    @PostMapping("/{id}/answers")
    @ResponseStatus(HttpStatus.OK)
    public A submitAnswer(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Sub") String sub,
            @RequestBody @Valid SR request) {
        log.debug("POST /public-questions/{}/answers sub={}", id, sub);
        return submitAnswerUseCase.execute(id, sub, request);
    }
}
