package com.quizapp.quiz.controller;

import com.quizapp.quiz.dto.QuizResult;
import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.GeneratedQuestion;
import com.quizapp.quiz.model.QuizConfig;
import com.quizapp.quiz.model.QuizSession;
import com.quizapp.quiz.service.FallbackQuestionGenerator;
import com.quizapp.quiz.service.QuestionGenerator;
import com.quizapp.quiz.service.QuizConfigService;
import com.quizapp.quiz.service.QuizResultPersistenceService;
import com.quizapp.quiz.service.QuizScoreService;
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
    private QuestionGenerator questionGenerator;

    @MockitoBean
    private FallbackQuestionGenerator fallbackQuestionGenerator;

    @MockitoBean
    private QuizConfigService quizConfigService;

    @MockitoBean
    private QuizSessionService quizSessionService;

    @MockitoBean
    private QuizScoreService quizScoreService;

    @MockitoBean
    private QuizResultPersistenceService quizResultPersistenceService;

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
                .andExpect(jsonPath("$.questions[0].id").value(1))
                .andExpect(jsonPath("$.questions[0].question").value("Which statement best describes Java?"))
                .andExpect(jsonPath("$.questions[0].options[0]").value("Option A"))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.fallback").value(false));

        verify(questionGenerator).generate(config);
    }

    @Test
    void shouldUseFallbackWhenPrimaryGeneratorFails() throws Exception {
        QuizConfig config = new QuizConfig("Java", Difficulty.MEDIUM, 1);
        GeneratedQuestion fallbackQ = new GeneratedQuestion("Fallback question about Java?",
                List.of("A", "B", "C", "D"), "B");

        when(quizConfigService.getCurrent()).thenReturn(config);
        when(questionGenerator.generate(config)).thenThrow(new IllegalStateException("Gemini question generation failed"));
        when(fallbackQuestionGenerator.generate(config)).thenReturn(List.of(fallbackQ));
        when(quizSessionService.startSession(List.of(fallbackQ)))
                .thenReturn(new QuizSession("session-fallback", List.of(fallbackQ)));

        mockMvc.perform(post("/api/quiz/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-fallback"))
                .andExpect(jsonPath("$.questions.length()").value(1))
                .andExpect(jsonPath("$.questions[0].question").value("Fallback question about Java?"))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.fallback").value(true));

        verify(questionGenerator).generate(config);
        verify(fallbackQuestionGenerator).generate(config);
    }

    @Test
    void shouldUseFallbackWhenApiKeyMissing() throws Exception {
        QuizConfig config = new QuizConfig("Python", Difficulty.EASY, 2);
        GeneratedQuestion q1 = new GeneratedQuestion("Fallback Python q1?",
                List.of("A", "B", "C", "D"), "A");
        GeneratedQuestion q2 = new GeneratedQuestion("Fallback Python q2?",
                List.of("W", "X", "Y", "Z"), "Z");

        when(quizConfigService.getCurrent()).thenReturn(config);
        when(questionGenerator.generate(config))
                .thenThrow(new IllegalStateException("GEMINI_API_KEY environment variable is not set"));
        when(fallbackQuestionGenerator.generate(config)).thenReturn(List.of(q1, q2));
        when(quizSessionService.startSession(List.of(q1, q2)))
                .thenReturn(new QuizSession("session-fb-2", List.of(q1, q2)));

        mockMvc.perform(post("/api/quiz/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andExpect(jsonPath("$.fallback").value(true))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.questions[1].correctAnswer").doesNotExist());
    }

    @Test
    void shouldUseFallbackWhenNetworkFailure() throws Exception {
        QuizConfig config = new QuizConfig("Java", Difficulty.HARD, 1);
        GeneratedQuestion fallbackQ = new GeneratedQuestion("Hard fallback Java q?",
                List.of("A", "B", "C", "D"), "C");

        when(quizConfigService.getCurrent()).thenReturn(config);
        when(questionGenerator.generate(config))
                .thenThrow(new IllegalStateException("Gemini question generation failed",
                        new java.net.UnknownHostException("api.generativeai.google.com")));
        when(fallbackQuestionGenerator.generate(config)).thenReturn(List.of(fallbackQ));
        when(quizSessionService.startSession(List.of(fallbackQ)))
                .thenReturn(new QuizSession("session-fb-net", List.of(fallbackQ)));

        mockMvc.perform(post("/api/quiz/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fallback").value(true))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist());

        verify(fallbackQuestionGenerator).generate(config);
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
                                  "questionId": 1,
                                  "selectedAnswer": "Option A"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(1))
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
                .when(quizSessionService).submitAnswer(eq("session-1"), eq(98), anyString());

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
                                  "questionId": 1,
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
                                  "questionId": 1,
                                  "selectedAnswer": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFinishSessionAndSaveResult() throws Exception {
        QuizConfig config = new QuizConfig("Java", Difficulty.MEDIUM, 2);
        GeneratedQuestion q1 = new GeneratedQuestion("What is Java?",
                List.of("Option A", "Option B", "Option C", "Option D"), "Option A");
        GeneratedQuestion q2 = new GeneratedQuestion("What is Spring?",
                List.of("Option W", "Option X", "Option Y", "Option Z"), "Option Z");
        QuizSession session = new QuizSession("session-1", List.of(q1, q2));
        session.submitAnswer(0, "Option A");
        session.submitAnswer(1, "Option X");
        QuizResult result = new QuizResult("session-1", 2, 2, 1, 1, 1, 50.0);

        when(quizConfigService.getCurrent()).thenReturn(config);
        when(quizSessionService.getSession("session-1")).thenReturn(session);
        when(quizScoreService.calculate(session)).thenReturn(result);
        when(quizResultPersistenceService.save(result, "Java", Difficulty.MEDIUM)).thenReturn(result);

        mockMvc.perform(post("/api/quiz/sessions/session-1/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.answeredQuestions").value(2))
                .andExpect(jsonPath("$.correctAnswers").value(1))
                .andExpect(jsonPath("$.wrongAnswers").value(1))
                .andExpect(jsonPath("$.score").value(1))
                .andExpect(jsonPath("$.percentage").value(50.0))
                .andExpect(jsonPath("$.wrongAnswersList.length()").value(1))
                .andExpect(jsonPath("$.wrongAnswersList[0].questionNumber").value(2))
                .andExpect(jsonPath("$.wrongAnswersList[0].question").value("What is Spring?"))
                .andExpect(jsonPath("$.wrongAnswersList[0].yourAnswer").value("Option X"))
                .andExpect(jsonPath("$.wrongAnswersList[0].correctAnswer").value("Option Z"));

        verify(quizScoreService).calculate(session);
        verify(quizResultPersistenceService).save(result, "Java", Difficulty.MEDIUM);
    }

    @Test
    void shouldReturnEmptyWrongAnswersListWhenAllCorrect() throws Exception {
        QuizConfig config = new QuizConfig("Java", Difficulty.EASY, 1);
        GeneratedQuestion q1 = new GeneratedQuestion("What is Java?",
                List.of("Option A", "Option B", "Option C", "Option D"), "Option A");
        QuizSession session = new QuizSession("session-1", List.of(q1));
        session.submitAnswer(0, "Option A");
        QuizResult result = new QuizResult("session-1", 1, 1, 1, 0, 1, 100.0);

        when(quizConfigService.getCurrent()).thenReturn(config);
        when(quizSessionService.getSession("session-1")).thenReturn(session);
        when(quizScoreService.calculate(session)).thenReturn(result);
        when(quizResultPersistenceService.save(result, "Java", Difficulty.EASY)).thenReturn(result);

        mockMvc.perform(post("/api/quiz/sessions/session-1/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctAnswers").value(1))
                .andExpect(jsonPath("$.wrongAnswers").value(0))
                .andExpect(jsonPath("$.wrongAnswersList.length()").value(0));

        verify(quizScoreService).calculate(session);
    }

    @Test
    void shouldReturn404WhenFinishingUnknownSession() throws Exception {
        when(quizSessionService.getSession("unknown"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz session not found"));

        mockMvc.perform(post("/api/quiz/sessions/unknown/finish"))
                .andExpect(status().isNotFound());
    }
}
