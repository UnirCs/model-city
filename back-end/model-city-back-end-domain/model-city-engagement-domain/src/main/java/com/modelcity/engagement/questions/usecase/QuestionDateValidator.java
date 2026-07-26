package com.modelcity.engagement.questions.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/** Shared validation for civic question write operations. */
final class QuestionDateValidator {

    private QuestionDateValidator() {}

    /** Validates that openDate is future and closeDate is at least 3 days after openDate. */
    static void validateDates(LocalDate openDate, LocalDate closeDate) {
        if (!openDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Open date must be after today");
        }
        if (closeDate.isBefore(openDate.plusDays(3))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Close date must be at least 3 days after open date");
        }
    }
}
