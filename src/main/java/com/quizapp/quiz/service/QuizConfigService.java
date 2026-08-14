package com.quizapp.quiz.service;

import com.quizapp.quiz.model.QuizConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuizConfigService {

    private volatile QuizConfig currentConfig;

    public QuizConfig save(QuizConfig config) {
        this.currentConfig = config;
        return config;
    }

    public QuizConfig getCurrent() {
        if (currentConfig == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz configuration has been set yet");
        }
        return currentConfig;
    }
}
