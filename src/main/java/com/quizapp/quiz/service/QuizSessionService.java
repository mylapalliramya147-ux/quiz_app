package com.quizapp.quiz.service;

import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuizSessionService {

    private final Map<String, QuizSession> sessions = new ConcurrentHashMap<>();

    public QuizSession startSession(List<GeneratedQuestion> questions) {
        QuizSession session = new QuizSession(UUID.randomUUID().toString(), questions);
        sessions.put(session.id(), session);
        return session;
    }

    public QuizSession getSession(String sessionId) {
        QuizSession session = sessions.get(sessionId);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz session not found");
        }
        return session;
    }

    public void submitAnswer(String sessionId, int questionId, String answer) {
        QuizSession session = getSession(sessionId);
        if (questionId < 0 || questionId >= session.questions().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid question id");
        }
        if (!session.questions().get(questionId).options().contains(answer)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer must be one of the question options");
        }
        session.submitAnswer(questionId, answer);
    }
}
