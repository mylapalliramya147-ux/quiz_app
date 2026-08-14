package com.quizapp.quiz.service;

import com.quizapp.quiz.model.Difficulty;
import com.quizapp.quiz.model.Question;
import com.quizapp.quiz.model.QuizConfig;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class MockQuestionGenerator implements QuestionGenerator {

    private static final Map<Difficulty, List<Template>> TEMPLATES = Map.of(
            Difficulty.EASY, List.of(
                    new Template("Which statement best describes %s?",
                            List.of("%s is the set of core principles, concepts, and practices of the field",
                                    "%s is an unrelated hobby with no practical use",
                                    "%s refers only to a single historical event",
                                    "%s cannot be described at all"),
                            0),
                    new Template("What is the most basic building block of %s?",
                            List.of("A random guess",
                                    "Its fundamental concepts and terminology",
                                    "Advanced algorithms that only experts know",
                                    "An unrelated topic"),
                            1),
                    new Template("Why do people study %s?",
                            List.of("For no reason at all",
                                    "Because it cannot be learned",
                                    "To understand its principles and apply them",
                                    "To avoid doing useful work"),
                            2)
            ),
            Difficulty.MEDIUM, List.of(
                    new Template("Which practice in %s is most commonly used in real projects?",
                            List.of("Standard industry patterns and conventions",
                                    "Ignoring all established practices",
                                    "Using only theoretical ideas",
                                    "Avoiding documentation"),
                            0),
                    new Template("What is the purpose of applying %s to a real problem?",
                            List.of("To make the problem harder",
                                    "To solve the problem more effectively",
                                    "To hide the problem",
                                    "To create new problems"),
                            1),
                    new Template("In %s, what does a solid solution usually rely on?",
                            List.of("Pure luck",
                                    "Unwritten secrets",
                                    "Established guidelines and experience",
                                    "Random chance"),
                            2)
            ),
            Difficulty.HARD, List.of(
                    new Template("Which advanced technique in %s is considered a best practice?",
                            List.of("Applying proven patterns while avoiding premature optimization",
                                    "Rewriting everything from scratch daily",
                                    "Ignoring performance entirely",
                                    "Avoiding all tools and libraries"),
                            0),
                    new Template("What distinguishes an expert approach in %s from a beginner one?",
                            List.of("Copying the first example found",
                                    "Understanding trade-offs and choosing deliberately",
                                    "Avoiding all analysis",
                                    "Using the most complex option available"),
                            1),
                    new Template("In %s, when is a sophisticated solution preferable?",
                            List.of("When it is required by the problem, not merely preferred",
                                    "Always, regardless of the problem",
                                    "Only when it makes the code harder to read",
                                    "Never, simple is always wrong"),
                            0)
            )
    );

    @Override
    public List<Question> generate(QuizConfig config) {
        return IntStream.range(0, config.numQuestions())
                .mapToObj(index -> buildQuestion(config, index))
                .toList();
    }

    private Question buildQuestion(QuizConfig config, int index) {
        List<Template> templates = TEMPLATES.get(config.difficulty());
        Template template = templates.get(index % templates.size());
        String questionText = fill(template.stem(), config.topic());
        List<String> options = template.options().stream()
                .map(option -> fill(option, config.topic()))
                .toList();
        return new Question(questionText, options, options.get(template.correctIndex()));
    }

    private static String fill(String text, String topic) {
        return text.replace("%s", topic);
    }

    private record Template(String stem, List<String> options, int correctIndex) {
    }
}
