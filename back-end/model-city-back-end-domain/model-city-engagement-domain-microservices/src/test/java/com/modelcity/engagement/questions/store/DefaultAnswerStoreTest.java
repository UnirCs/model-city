package com.modelcity.engagement.questions.store;

import com.modelcity.engagement.questions.repository.AnswerRepository;
import com.modelcity.engagement.questions.repository.CivicQuestionRepository;
import com.modelcity.engagement.questions.repository.model.Answer;
import com.modelcity.engagement.questions.repository.model.CivicQuestion;
import com.modelcity.engagement.questions.repository.model.Vote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAnswerStoreTest {

    @Mock
    AnswerRepository<Answer> answerRepository;

    @Mock
    CivicQuestionRepository<CivicQuestion> civicQuestionRepository;

    DefaultAnswerStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultAnswerStore(answerRepository, civicQuestionRepository);
    }

    @Test
    void hasAnswered_delegatesToRepository() {
        when(answerRepository.existsByQuestionIdAndCitizenId(1L, "citizen-sub")).thenReturn(true);
        assertThat(store.hasAnswered(1L, "citizen-sub")).isTrue();
    }

    @Test
    void hasVoted_delegatesToRepository() {
        when(answerRepository.existsByQuestionIdAndDniHash(1L, "dni-hash")).thenReturn(true);
        assertThat(store.hasVoted(1L, "dni-hash")).isTrue();
    }

    @Test
    void createAnswer_withYesVote_incrementsYesCount() {
        CivicQuestion question = CivicQuestion.builder().build();
        when(civicQuestionRepository.getReferenceById(1L)).thenReturn(question);
        Answer saved = Answer.builder().id(100L).build();
        when(answerRepository.save(any(Answer.class))).thenReturn(saved);

        Long answerId = store.createAnswer(1L, "citizen-sub", "dni-hash", Vote.YES);

        assertThat(answerId).isEqualTo(100L);
        verify(civicQuestionRepository).incrementYesCount(1L);
        verify(civicQuestionRepository, never()).incrementNoCount(any());
    }

    @Test
    void createAnswer_withNoVote_incrementsNoCount() {
        CivicQuestion question = CivicQuestion.builder().build();
        when(civicQuestionRepository.getReferenceById(1L)).thenReturn(question);
        Answer saved = Answer.builder().id(101L).build();
        when(answerRepository.save(any(Answer.class))).thenReturn(saved);

        Long answerId = store.createAnswer(1L, "citizen-sub", "dni-hash", Vote.NO);

        assertThat(answerId).isEqualTo(101L);
        verify(civicQuestionRepository).incrementNoCount(1L);
        verify(civicQuestionRepository, never()).incrementYesCount(any());
    }
}
