package com.quizapp.quiz.repository;

import com.quizapp.quiz.model.QuizResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResultRepository extends JpaRepository<QuizResultEntity, Long> {
}
