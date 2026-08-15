package com.quizapp.quiz.controller;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.model.QuizSession;
import com.quizapp.quiz.service.QuestionGenerator;
import com.quizapp.quiz.service.QuizConfigService;
import com.quizapp.quiz.service.QuizSessionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizSessionController.class)
class QuizSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizConfigService quizConfigService;

    @MockitoBean
    private QuestionGenerator questionGenerator;

    @MockitoBean
    private QuizSessionService quizSessionService;

    @Test
    void shouldStartQuizUsingCurrentConfigAndHideCorrectAnswers() throws Exception {
        QuizConfig config = new QuizConfig("Java", Difficulty.MEDIUM, 1);
        GeneratedQuestion question = new GeneratedQuestion("Which statement best describes Java?",
                List.of("Option A", "Option B", "Option C", "Option D"), "Option A");

        when(quizConfigService.getCurrent()).thenReturn(config);
        when(questionGenerator.generate(config)).thenReturn(List.of(question));
        when(quizSessionService.startSession(List.of(question)))
                .thenReturn(new QuizSession("session-1", List.of(question)));

        mockMvc.perform(post("/api/quiz/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.questions.length()").value(1))
                .andExpect(jsonPath("$.questions[0].id").value(0))
                .andExpect(jsonPath("$.questions[0].question").value("Which statement best describes Java?"))
                .andExpect(jsonPath("$.questions[0].options[0]").value("Option A"))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist());

        verify(questionGenerator).generate(config);
    }

    @Test
    void shouldRetrieveQuestionsWithoutCorrectAnswers() throws Exception {
        GeneratedQuestion question = new GeneratedQuestion("What is Java?",
                List.of("Option A", "Option B", "Option C", "Option D"), "Option B");
        when(quizSessionService.getSession("session-1"))
                .thenReturn(new QuizSession("session-1", List.of(question)));

        mockMvc.perform(get("/api/quiz/sessions/session-1/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.questions.length()").value(1))
                .andExpect(jsonPath("$.questions[0].question").value("What is Java?"))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist());
    }

    @Test
    void shouldSubmitAnswer() throws Exception {
        mockMvc.perform(post("/api/quiz/sessions/session-1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionId": 0,
                                  "selectedAnswer": "Option A"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(0))
                .andExpect(jsonPath("$.selectedAnswer").value("Option A"));

        verify(quizSessionService).submitAnswer("session-1", 0, "Option A");
    }

    @Test
    void shouldReturn404ForUnknownSession() throws Exception {
        when(quizSessionService.getSession("unknown"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz session not found"));

        mockMvc.perform(get("/api/quiz/sessions/unknown/questions"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForInvalidQuestionId() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid question id"))
                .when(quizSessionService).submitAnswer(eq("session-1"), eq(99), anyString());

        mockMvc.perform(post("/api/quiz/sessions/session-1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionId": 99,
                                  "selectedAnswer": "Option A"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForInvalidAnswer() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer must be one of the question options"))
                .when(quizSessionService).submitAnswer(eq("session-1"), eq(0), eq("Not an option"));

        mockMvc.perform(post("/api/quiz/sessions/session-1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionId": 0,
                                  "selectedAnswer": "Not an option"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForBlankAnswer() throws Exception {
        mockMvc.perform(post("/api/quiz/sessions/session-1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionId": 0,
                                  "selectedAnswer": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
