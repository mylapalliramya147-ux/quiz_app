package com.quizapp.quiz.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
public class QuizResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private int answeredQuestions;

    @Column(nullable = false)
    private int correctAnswers;

    @Column(nullable = false)
    private int wrongAnswers;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private double percentage;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    protected QuizResultEntity() {
    }

    public QuizResultEntity(String sessionId, String topic, Difficulty difficulty,
                            int totalQuestions, int answeredQuestions, int correctAnswers,
                            int wrongAnswers, int score, double percentage, LocalDateTime completedAt) {
        this.sessionId = sessionId;
        this.topic = topic;
        this.difficulty = difficulty;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.score = score;
        this.percentage = percentage;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTopic() {
        return topic;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public int getScore() {
        return score;
    }

    public double getPercentage() {
        return percentage;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
