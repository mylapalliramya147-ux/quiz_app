package com.quizapp.quiz.service;

import com.quizapp.quiz.dto.QuizResult;
import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.QuizResultEntity;
import com.quizapp.quiz.repository.QuizResultRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuizResultPersistenceServiceTest {

    @Test
    void saveMapsResultToEntityAndPersists() {
        QuizResultRepository repository = mock(QuizResultRepository.class);
        QuizResultPersistenceService service = new QuizResultPersistenceService(repository);
        QuizResult result = new QuizResult("session-1", 5, 4, 3, 2, 3, 60.0);
        when(repository.save(any(QuizResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuizResult saved = service.save(result, "Java", Difficulty.MEDIUM);

        ArgumentCaptor<QuizResultEntity> captor = ArgumentCaptor.forClass(QuizResultEntity.class);
        verify(repository).save(captor.capture());
        QuizResultEntity entity = captor.getValue();

        assertEquals("session-1", entity.getSessionId());
        assertEquals("Java", entity.getTopic());
        assertEquals(Difficulty.MEDIUM, entity.getDifficulty());
        assertEquals(5, entity.getTotalQuestions());
        assertEquals(4, entity.getAnsweredQuestions());
        assertEquals(3, entity.getCorrectAnswers());
        assertEquals(2, entity.getWrongAnswers());
        assertEquals(3, entity.getScore());
        assertEquals(60.0, entity.getPercentage(), 0.001);
        assertNotNull(entity.getCompletedAt());

        assertEquals(result, saved);
    }

    @Test
    void saveReturnsResultMappedFromSavedEntity() {
        QuizResultRepository repository = mock(QuizResultRepository.class);
        QuizResultPersistenceService service = new QuizResultPersistenceService(repository);
        QuizResult result = new QuizResult("session-2", 2, 2, 1, 1, 1, 50.0);
        when(repository.save(any(QuizResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuizResult saved = service.save(result, "Spring", Difficulty.HARD);

        assertEquals("session-2", saved.sessionId());
        assertEquals(2, saved.totalQuestions());
        assertEquals(2, saved.answeredQuestions());
        assertEquals(1, saved.correctAnswers());
        assertEquals(1, saved.wrongAnswers());
        assertEquals(1, saved.score());
        assertEquals(50.0, saved.percentage(), 0.001);
    }
}
