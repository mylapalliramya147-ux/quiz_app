package com.quizapp.quiz.service;

import com.quizapp.quiz.dto.QuizResult;
import com.quizapp.quiz.model.QuizSession;
import org.springframework.stereotype.Service;

@Service
public class QuizScoreService {

    public QuizResult calculate(QuizSession session) {
        int total = session.questions().size();
        int correct = 0;
        for (int i = 0; i < total; i++) {
            String submitted = session.answers().get(i);
            if (submitted != null && submitted.equals(session.questions().get(i).correctAnswer())) {
                correct++;
            }
        }
        int answered = session.answers().size();
        int wrong = total - correct;
        double percentage = total == 0 ? 0.0 : Math.round(correct * 10000.0 / total) / 100.0;
        return new QuizResult(session.id(), total, answered, correct, wrong, correct, percentage);
    }
}
