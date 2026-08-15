package com.quizapp.quiz.service;

import com.quizapp.quiz.dto.QuizResult;
import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.QuizResultEntity;
import com.quizapp.quiz.repository.QuizResultRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QuizResultPersistenceService {

    private final QuizResultRepository repository;

    public QuizResultPersistenceService(QuizResultRepository repository) {
        this.repository = repository;
    }

    public QuizResult save(QuizResult result, String topic, Difficulty difficulty) {
        QuizResultEntity entity = repository.save(toEntity(result, topic, difficulty));
        return toResult(entity);
    }

    private QuizResultEntity toEntity(QuizResult result, String topic, Difficulty difficulty) {
        return new QuizResultEntity(result.sessionId(), topic, difficulty,
                result.totalQuestions(), result.answeredQuestions(), result.correctAnswers(),
                result.wrongAnswers(), result.score(), result.percentage(), LocalDateTime.now());
    }

    private QuizResult toResult(QuizResultEntity entity) {
        return new QuizResult(entity.getSessionId(), entity.getTotalQuestions(),
                entity.getAnsweredQuestions(), entity.getCorrectAnswers(),
                entity.getWrongAnswers(), entity.getScore(), entity.getPercentage());
    }
}
