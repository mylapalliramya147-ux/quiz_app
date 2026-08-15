package com.quizapp.quiz.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuizSession {

    private final String id;
    private final List<GeneratedQuestion> questions;
    private final Map<Integer, String> answers = new ConcurrentHashMap<>();

    public QuizSession(String id, List<GeneratedQuestion> questions) {
        this.id = id;
        this.questions = List.copyOf(questions);
    }

    public String id() {
        return id;
    }

    public List<GeneratedQuestion> questions() {
        return questions;
    }

    public void submitAnswer(int questionId, String answer) {
        answers.put(questionId, answer);
    }

    public Map<Integer, String> answers() {
        return Map.copyOf(answers);
    }
}
